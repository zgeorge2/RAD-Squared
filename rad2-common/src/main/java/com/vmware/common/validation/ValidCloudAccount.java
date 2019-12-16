package com.vmware.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {CloudAccountValidator.class})
public @interface ValidCloudAccount {
    String message() default
        "Cloud account validation failed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
