package com.devteria.Demo_Spring_boot.service;

import com.devteria.Demo_Spring_boot.dto.request.AuthenticationRequest;
import com.devteria.Demo_Spring_boot.dto.request.IntrospectRequest;
import com.devteria.Demo_Spring_boot.dto.response.AuthenticationResponse;
import com.devteria.Demo_Spring_boot.dto.response.IntrospectResponse;
import com.devteria.Demo_Spring_boot.entity.User;
import com.devteria.Demo_Spring_boot.exception.AppException;
import com.devteria.Demo_Spring_boot.exception.ErrorCode;
import com.devteria.Demo_Spring_boot.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository; //DI

    @NonFinal// không DI
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        return IntrospectResponse.builder()
                .valid(verified && expityTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated){ //nếu không xác thực sẽ báo lỗi
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }
    private String generateToken(User user){ //yêu cầu 2 params đó là Header và Payload
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); //b1 tạo thuật toán tạo token, ở đây là HS512

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() //đây là body gửi đi
                .subject(user.getUsername()) //usrname, có thể thêm 1 số subject khác
                .issuer("devteria.com")//từ domain
                .issueTime(new Date())//thời gian tạo token
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))//tạo token 1 giờ
                .claim("scope", buildScope(user))//claim thêm role user
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); //sao khi có header tạo payload để bỏ vào jwsobject

        JWSObject jwsObject = new JWSObject(header, payload);// truyền vào 2 params ở trên

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));//ký giải mã chuỗi 32bit ngẫu nhiên
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Can't create token", e);
            throw new RuntimeException(e);
        }
    }
    private String buildScope(User user){//thêm buildScope phân quyền người dùng như admin, khách, ...
        StringJoiner stringJoiner = new StringJoiner(" ");//mặc định của stringJoiner là dấu cách, ở đây các scope là string nên sẽ dùng stringjoiner
        if (!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());//để phân biệt ROLE_ và Permission
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }
        return stringJoiner.toString();
    }
}
