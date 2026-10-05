package com.faefluffkrist.campfireward;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigOption {
    String label();
    String description();
    double min() default 0;
    double max() default 128;
}
