package it.theapplegeek.spring_starter_pack.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzJob {
  String name();

  String group() default "DEFAULT";

  String description() default "";

  JobType jobType();

  String cronExpressionConfigKey() default "";

  String defaultCronExpression() default "";

  enum JobType {
    APPLICATION_CONFIG,
    USER_CONFIG
  }
}
