package com.devteria.Demo_Spring_boot.exception;

import com.devteria.Demo_Spring_boot.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice //Annotation này để bắt lỗi toàn cục thay vì viết try-catch cho từng API và trả về phản hồi tùy chỉnh khi có lỗi

public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";//lấy giá trị min từ



    @ExceptionHandler(value = Exception.class) //xử lý lỗi ngoại lệ
//      bài cũ
//    ResponseEntity<String> handlingRuntimeException(RuntimeException exception){
//        return ResponseEntity.badRequest().body(exception.getMessage());

    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception){
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());//trả về code báo lỗi
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode(); //Lấy ra error code có 2 tham số code và message từ class ErrorCode
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());//trả về code báo lỗi
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class) // thay vì trả về 1 api thì trả về 1 reponse
//    ResponseEntity<String> handlingValidation(MethodArgumentNotValidException exception){
//        return ResponseEntity.badRequest().body(exception.getFieldError().getDefaultMessage());
//    }
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumkey = exception.getFieldError().getDefaultMessage();
//        ErrorCode errorCode = ErrorCode.valueOf(enumkey);
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try{ //bắt lỗi nếu truyền sai tên, ví dụ PASSWORD_INVALID thành PASSWORD_Ivalid sẽ hiển thị lỗi 1001
            errorCode = ErrorCode.valueOf(enumkey);

            var contrainViolation = exception.getBindingResult()
                    .getAllErrors().getFirst().unwrap(ConstraintViolation.class); //getBindingResult() là những exception của MethodArgumentNotValidException wrap lại

            /*
            * contrainViolation lấy lỗi đầu tiên trong validation bằng getFirst() sau đó dùng unwrap để chuyển thành object
            *
            * attrubutes sẽ trả về một Map<String, object> chứa các thuộc tính của annotation trong đó có min value
            * */

            attributes = contrainViolation.getConstraintDescriptor().getAttributes();


        }catch (IllegalArgumentException e){

        }

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());//trả về code báo lỗi
        apiResponse.setMessage(Objects.nonNull(attributes) ?
                mapAttribute(errorCode.getMessage(), attributes) :
                errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes){
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));//vì min ở đây theo int nên phải ép kiểu sang String
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

}
