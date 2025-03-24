package com.devteria.Demo_Spring_boot.dto.request;


import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE) //tạo nhanh mức độ truy cập giữa các class, ở đây là Private
public class PasswordCreationRequest {
    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;
}
