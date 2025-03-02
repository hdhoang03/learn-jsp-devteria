package com.devteria.Demo_Spring_boot.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {// ở đây truyền vào dạng LocalDay để validate ngày sinh, nếu validate ký tự thì truyền String vào

    private int min;

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        //mỗi annotation chỉ chịu trách nhiệm cho một validation cụ thể
        if(Objects.isNull(localDate))
            return true;

        long years = ChronoUnit.YEARS.between(localDate, LocalDate.now());//so sánh giữa ngày nhập vào và ngày hiện tại coi đủ 18 chưa

        return years >= min;//trả về ngày sinh lớn hơn hoặc bằng min
    }

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }
}
