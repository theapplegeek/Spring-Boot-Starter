package it.theapplegeek.spring_starter_pack.common.annotation;

import it.theapplegeek.spring_starter_pack.common.annotation.validator.FileTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileTypeValidator.class)
public @interface ValidFileType {
  String message() default "Invalid file type";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] types();
}
