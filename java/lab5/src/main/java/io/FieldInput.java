package io;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FieldInput {
  String prompt() default "";

  String errorMessage() default "";

  int flags() default 0b000;
  double min() default Double.NEGATIVE_INFINITY;
  double max() default Double.POSITIVE_INFINITY;
}
