package com.devteria.Demo_Spring_boot.configuration;

import com.devteria.Demo_Spring_boot.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Configuration
@EnableWebSecurity//spring security tự enable rồi nên có hoặc không cũng được
@EnableMethodSecurity //Phân quyền trên method

public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS =
            {"/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"}; //truyền các API vào nhanh hơn, các API này truy cập không cần token như đăng ký tài khoản mới

    //ko cần vì đã map trong customJwtDecoder
//    @Value("${jwt.signerKey}") //map signerKey từ file application.yaml vào trong này
//    private String signerKey;

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.authorizeHttpRequests(request ->
//                request.requestMatchers(HttpMethod.POST, "/users").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/auth/token", "/auth/introspect").permitAll()
                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() //truyền vào đây
//                        .requestMatchers(HttpMethod.GET, "/users")
//                        .hasAuthority("ROLE_ADMIN")//get mà có scope là admin thì mới có thể get được thông tin user, mặc định là scope_ mình đã đặt lại thành role_ rồi lấy scope gán vào hậu tố
//                        .hasRole(Role.ADMIN.name())//lấy role trong authority luôn không cần dùng hasAuthority
                        .anyRequest().authenticated());//còn lại các request khác phải dùng token

        httpSecurity.oauth2ResourceServer(oauth2 ->
            oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))// cần truyền jwtDecoder vào nên định nghĩa hàm ở dưới
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                /*
                * .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) dùng 1 lần nên không cần di vào
                * authentication fail trả về 1 error message chứ không cần điều hướng
                *  */
        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);//tắt bảo vệ csrf vì API REST không cần csrf

        httpSecurity.cors(Customizer.withDefaults());//Kết nối fe
        return httpSecurity.build();
    }

    //có customJwtDecoder rồi nên không dùng
//    @Bean
//    JwtDecoder jwtDecoder(){ //xác thực JWT sử dụng khóa bí mật signerKey
//        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(),"HS512");//truyền thuật toán mã hóa vào
//        return NimbusJwtDecoder
//                .withSecretKey(secretKeySpec)
//                .macAlgorithm(MacAlgorithm.HS512)
//                .build();
//    }

    @Bean //đánh dấu 1 bean vì dùng nhiều nơi
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");//vì đã gắn ROLE vào trong AuthenticationService
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500")); // Đổi theo frontend của bạn http://127.0.0.1:5500
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
/*
* Luồng hoạt động của phân quyền hoạt động bởi JWT:
* 1. CLient gửi request API thì Spring security sẽ kiểm tra cái request đó
* 2. Nếu 1 trong các API thuộc trong PUBLIC_ENDPOINT thì không cần token người dùng có thể truy cập
* 3. Nếu nằm ngoài sẽ yêu cầu token JWT khóa bí mật
* 4. Spring security sẽ kiểm tra bằng hàm jwtDecoder() bằng thuật toán mã hóa, ở đây là HS512
* 5. Nếu token hợp lệ người dùng sẽ truy cập được API
* 6. Nếu token sai hoặc hết hạn sẽ báo lỗi 401 Unauthorized
*
* Có vẻ khá đầy đủ nhưng có một số trường hợp như phân quyền admin, người dùng thì thêm request.requestMatchers("/admin/**").hasRole("ADMIN"); có một số trường hợp đặc biệt nữa
* */