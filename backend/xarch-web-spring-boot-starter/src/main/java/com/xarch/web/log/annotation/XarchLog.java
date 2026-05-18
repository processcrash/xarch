package com.xarch.web.log.annotation;

import java.lang.annotation.*;

/**
 * Operation logging annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XarchLog {

    /**
     * Operation description
     */
    String value() default "";

    /**
     * Operation type
     */
    String type() default "OPERATION";

    /**
     * Target class
     */
    Class<?> targetClass() default void.class;
}