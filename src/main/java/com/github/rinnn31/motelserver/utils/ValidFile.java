package com.github.rinnn31.motelserver.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;

@Documented
@Constraint(validatedBy = ValidFileValidator.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "File không hợp lệ hoặc có định dạng không được hỗ trợ";
    Class<?>[] groups() default {};
    String[] allowedTypes() default {};
}
