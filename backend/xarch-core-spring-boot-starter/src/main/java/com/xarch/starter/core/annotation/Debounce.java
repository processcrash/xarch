package com.xarch.starter.core.annotation;

import java.lang.annotation.*;

/**
 * Debounce annotation to prevent duplicate submissions
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Debounce {

    /** Lock key prefix */
    String key() default "";

    /** Lock timeout in milliseconds */
    long timeout() default 3000;
}