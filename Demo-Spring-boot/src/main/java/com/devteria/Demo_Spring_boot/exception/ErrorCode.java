package com.devteria.Demo_Spring_boot.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter

public enum ErrorCode {
    INVALID_KEY(1001, "Invalid message key", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error.", HttpStatus.BAD_REQUEST),
    USER_NOTFOUND(1007, "User not found.", HttpStatus.NOT_FOUND),
    USER_EXISTED(1002, "User already existed.", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least {min} characters.", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed.", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated.", HttpStatus.UNAUTHORIZED),
    USER_LASTNAME(1008, "Last name not found.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1009, "You don't have permission.", HttpStatus.FORBIDDEN),
    INVALID_DOB(1010, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1011, "Email must have (@gmail.com)", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1012, "Email already existed.", HttpStatus.BAD_REQUEST),
    PASSWORD_EXISTED(1013, "Password existed", HttpStatus.BAD_REQUEST)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.statusCode = statusCode;
        this.message = message;
    }

    private int code;
    private HttpStatusCode statusCode;
    private String message;

}
