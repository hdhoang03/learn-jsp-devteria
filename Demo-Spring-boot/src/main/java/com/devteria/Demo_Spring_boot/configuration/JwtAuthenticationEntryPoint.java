package com.devteria.Demo_Spring_boot.configuration;

import com.devteria.Demo_Spring_boot.dto.request.ApiResponse;
import com.devteria.Demo_Spring_boot.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);//trả dữ liệu về cái gì

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        //Chuyển ApiResponse thành JSON phản hồi
        ObjectMapper objectMapper = new ObjectMapper();//chuyển đối tượng thành JSON
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));//convert apiResponse về chuỗi JSON và ghi JSON này gửi vào phản hồi HTTP
        response.flushBuffer();//Đảm bảo gửi dữ liệu ngay lập tức
    }
}
