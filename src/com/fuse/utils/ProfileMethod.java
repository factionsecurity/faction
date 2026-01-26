package com.fuse.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation annotation to mark methods that are being profiled.
 * This annotation is for documentation purposes only and has no runtime
 * behavior.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ProfileMethod {
    /**
     * Optional description of what is being profiled
     */
    String value() default "";
}