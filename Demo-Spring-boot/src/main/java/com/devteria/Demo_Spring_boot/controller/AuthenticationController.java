package com.devteria.Demo_Spring_boot.controller;

import com.devteria.Demo_Spring_boot.dto.request.ApiResponse;
import com.devteria.Demo_Spring_boot.dto.request.AuthenticationRequest;
import com.devteria.Demo_Spring_boot.dto.request.IntrospectRequest;
import com.devteria.Demo_Spring_boot.dto.response.AuthenticationResponse;
import com.devteria.Demo_Spring_boot.dto.response.IntrospectResponse;
import com.devteria.Demo_Spring_boot.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.catalina.util.Introspection;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor //autowired các bean nếu đánh dấu final
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // tự động chỉnh các thuộc tính thành private và đánh dấu final để trở thành các bean (?)
public class AuthenticationController {
    AuthenticationService authenticationService; //DI

    @PostMapping("/token")//log-in
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
//                .result(AuthenticationResponse.builder()
//                        .authenticated(result)
//                        .build())
                .result(result)
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
}
