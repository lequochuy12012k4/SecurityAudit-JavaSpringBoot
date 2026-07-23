package com.javasecurityaudit.jsa_core.config.annotation;

import com.javasecurityaudit.jsa_core.enums.AuditAction;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogActivity {
    AuditAction action();
    String description() default "";
}