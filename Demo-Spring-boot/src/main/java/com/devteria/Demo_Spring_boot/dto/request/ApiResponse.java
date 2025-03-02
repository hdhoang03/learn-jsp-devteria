package com.devteria.Demo_Spring_boot.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)//trả về Json không null
public class ApiResponse <T> {
    private int code = 1000; //api trả về giá trị 1000 là thành công
    private String message;
    private T result;
}
