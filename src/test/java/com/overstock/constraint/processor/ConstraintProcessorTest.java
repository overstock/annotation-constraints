package com.overstock.constraint.processor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.overstock.constraint.RequireNoArgConstructor;
import com.overstock.constraint.RequireStringLongIntArrayConstructor;

public class ConstraintProcessorTest extends AbstractCompilingTest {
  private static final String PACKAGE_NAME = "com.overstock.constraint";
  private static final String PACKAGE_DECLARATION = "package " + PACKAGE_NAME + ";";

  @Mock
  private Messager messager;

  private Processor wrapped;

  @Test
  public void testNoConstraints() throws Exception {
    SourceFile[] sourceFiles = { new SourceFile(
      filePath("SimpleAnnotated.java"),
      PACKAGE_DECLARATION,
      "@NoConstraints(someInt=3)",
      "public class SimpleAnnotated extends java.util.ArrayList {}") };

    assertCleanCompile(sourceFiles);
  }

  @Test
  public void testNoArgConstructorPass() throws Exception {
    SourceFile[] sourceFiles = { new SourceFile(
      filePath("Annotated.java"),
      PACKAGE_DECLARATION,
      "@RequireNoArgConstructor public class Annotated {}") };

    assertCleanCompile(sourceFiles);
  }

  @Test
  public void testNoArgConstructorFail() throws Exception {
    SourceFile[] sourceFiles = { new SourceFile(
      filePath("Annotated.java"),
      PACKAGE_DECLARATION,
      "@RequireNoArgConstructor public class Annotated { public Annotated(String s) {} }") };

    compile(sourceFiles);
    verifyPrintMessage(
      Diagnostic.Kind.ERROR,
      "Class " + className("Annotated") + " is annotated with @" + RequireNoArgConstructor.class.getName()
        + " but does not have a constructor with no arguments",
      new ClassValue(className("Annotated")),
      RequireNoArgConstructor.class);
    verifyNoMoreInteractions(messager);
  }

  @Test
  public void testRequireStringLongIntArrayConstructorPass() throws Exception {
    SourceFile[] sourceFiles = { new SourceFile(
      filePath("Annotated.java"),
      PACKAGE_DECLARATION,
      "@RequireStringLongIntArrayConstructor public class Annotated {",
      "  public Annotated(String s, long l, int[] array) {}",
      "}") };

    assertCleanCompile(sourceFiles);
  }

  @Test
  public void testRequireStringLongIntArrayConstructorFail() throws Exception {
    SourceFile[] sourceFiles = { new SourceFile(
      filePath("Annotated.java"),
      PACKAGE_DECLARATION,
      "@RequireStringLongIntArrayConstructor public class Annotated { ",
      "  public Annotated(String s, long l, long[] longs) {}",
      "}") };

    compile(sourceFiles);
    verifyPrintMessage(
      Diagnostic.Kind.ERROR,
      "Class " + className("Annotated") + " is annotated with @" + RequireStringLongIntArrayConstructor.class.getName()
        + " but does not have a constructor with arguments (java.lang.String, long, int[])",
      new ClassValue(className("Annotated")),
      RequireStringLongIntArrayConstructor.class);
    verifyNoMoreInteractions(messager);
  }

  private void compile(SourceFile[] sourceFiles) throws Exception {
    assertTrue("Compilation should succeed", compiler.compileWithProcessor(wrapped, sourceFiles));
  }

  private void assertCleanCompile(SourceFile[] sourceFiles) throws Exception {
    compile(sourceFiles);
    verifyZeroInteractions(messager); //no warnings or errors of the annotation processor
  }

  private void verifyPrintMessage(
    Diagnostic.Kind kind, String message, ClassValue elementClassValue, Class<?> annotationMirrorClass) {
    verify(messager).printMessage(
      eq(kind),
      eq(message),
      argThat(Matchers.<Element>hasToString(elementClassValue.getClassName())),
      argThat(Matchers.<AnnotationMirror>hasToString("@" + annotationMirrorClass.getName())));
  }

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    wrapped = new ProcessorWrapper(new ConstraintProcessor(), messager);
  }

  private static String className(String simpleName) {
    return PACKAGE_NAME + "." + simpleName;
  }

  private static String filePath(String fileName) {
    return PACKAGE_NAME.replaceAll("\\.", "/") + "/" + fileName;
  }

}
