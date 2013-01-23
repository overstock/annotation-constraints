package com.overstock.constraint;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires that annotated types have specific annotations.
 */
@Constraint
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.ANNOTATION_TYPE)
public @interface RequireAnnotations {

  /**
   * An array of annotations which MUST be present on any class annotated with the annotated
   * annotation. If {@code SomeAnnotation} is annotated with {@code RequireAnnotations}
   * where {@code value} is set to a array of annotation
   * classes which includes {@code SomeOtherAnnotation.class}, then it will be an error whenever
   * a class annotated with {@code @SomeAnnotation} is not also annotated with
   * {@code @SomeOtherAnnotation}.
   */
  Class<? extends Annotation>[] value();

}