package com.xarch.starter.core.annotation;

import java.lang.annotation.*;

/**
 * Validates that the field is not zero
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotZero {

    String message() default "Value must not be zero";
}