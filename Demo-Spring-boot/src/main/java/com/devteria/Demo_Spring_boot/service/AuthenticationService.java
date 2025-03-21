package com.devteria.Demo_Spring_boot.service;

import com.devteria.Demo_Spring_boot.constaint.PredefinedRole;
import com.devteria.Demo_Spring_boot.dto.request.*;
import com.devteria.Demo_Spring_boot.dto.response.AuthenticationResponse;
import com.devteria.Demo_Spring_boot.dto.response.IntrospectResponse;
import com.devteria.Demo_Spring_boot.entity.InvalidatedToken;
import com.devteria.Demo_Spring_boot.entity.Role;
import com.devteria.Demo_Spring_boot.entity.User;
import com.devteria.Demo_Spring_boot.exception.AppException;
import com.devteria.Demo_Spring_boot.exception.ErrorCode;
import com.devteria.Demo_Spring_boot.repository.InvalidatedTokenRepository;
import com.devteria.Demo_Spring_boot.repository.httpclient.OutboundIdentityClient;
import com.devteria.Demo_Spring_boot.repository.UserRepository;
import com.devteria.Demo_Spring_boot.repository.httpclient.OutboundUserClient;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository; //DI
    InvalidatedTokenRepository invalidatedTokenRepository;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;

    @NonFinal// không DI
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal// không DI
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal// không DI
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;

    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        }catch (AppException e){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse outboundAuthenticate(String code){
        var reponse = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                        .code(code)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .redirectUri(REDIRECT_URI)
                        .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE {}", reponse);

        var userInfo = outboundUserClient.getUserInfo("json", reponse.getAccessToken());

        log.info("User info {}", userInfo);

        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                .build());

        var user = userRepository.findByUsername(userInfo.getEmail()).orElseGet(
                ()-> userRepository.save(User.builder()
                                .username(userInfo.getEmail())
                                .firstName(userInfo.getGivenName())
                                .lastName(userInfo.getFamilyName())
                                .email(userInfo.getEmail())
                                .roles(roles)
                        .build()));

        return AuthenticationResponse.builder()
                .token(reponse.getAccessToken())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        log.info("SignKey: {}", SIGNER_KEY);

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

    public void logOut(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        }catch (AppException exception){
            log.info("Token already expired.");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {

        var signJWT = verifyToken(request.getToken(), true);
        var jit = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(()-> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);//tạo token dựa trên user
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime() //issue + refresh_duration (seconds)
                    .toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
// Nếu true thì verify để refresh token, false thì verify cho authenticate hoặc introspect

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        //nếu token tồn tại thì trả lỗi
        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String generateToken(User user){ //yêu cầu 2 params đó là Header và Payload
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); //b1 tạo thuật toán tạo token, ở đây là HS512

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() //đây là body gửi đi
                .subject(user.getUsername()) //usrname, có thể thêm 1 số subject khác
                .issuer("devteria.com")//từ domain
                .issueTime(new Date())//thời gian tạo token
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))//tạo token 1 giờ
                .jwtID(UUID.randomUUID().toString())
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
