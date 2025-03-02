package com.devteria.Demo_Spring_boot.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {DobValidator.class}// truyền vào class để thực hiện validate
)//class chịu trách nhiệm validate cho annotation nào
public @interface DobConstraint { //Khai báo @interface để định nghĩa 1 annotation tự tạo
    String message() default "Invalid date of birth";

    int min();//giá trị min của tuổi, có thể truyền ở đây hoặc bên các class dùng annotation này

    Class<?>[] groups() default{};

    Class<? extends Payload>[] payload() default {};

    //3 trường mặc định
}
/*
* đầu tiên sẽ built một interface, khai báo các properties
* sau đó sẽ built 1 validator implements cái annotation mới tạo
*/
