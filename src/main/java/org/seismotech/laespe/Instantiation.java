package org.seismotech.laespe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodType;
import static java.lang.invoke.MethodType.methodType;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.LambdaMetafactory;
import java.util.Arrays;

/**
 * An utility interface for object instantiation.
 */
public interface Instantiation {

  /**
   * <p>To use this on a specialized class returned by
   * {@link HierarchySpecializer},
   * {@code Instantiation.Metafactory.class} must be added to
   * the specialized class set
   * (typically adding {@code Instantiation.class} as a root to a
   * {@link HierarchyClassSet}).
   * Otherwise, this factory will search homonymous class in its class loader,
   * returning a factory for the original class instead of the specialized one.
   * See {@link BoundedSpecializerTest#wideHierarchyFactoryTest}
   * for an example avoiding or adding {@code Instantiation.class}.
   */
  static <T> T fastFactory(Class<?> objClass,
      Class<T> factClass, String factMethodName, Class<?>... argTypes)
  throws InstantiationRelatedException {
    return Metafactory.forClass(objClass)
      .factory(objClass, factClass, factMethodName, argTypes);
  }

  //----------------------------------------------------------------------
  /**
   * Dynamically creates an implementation for the functional factory
   * interface {@code factClass}.
   * The implementation will constructing objects of type {@code objClass}
   * using the constructor expecting exactly {@code argTypes}.
   * The implemented interface is expected to have a method called
   * {@code factMethodName}
   * accepting {@code argTypes} or more general types
   * and returning a {@code objClass} or a more general type.
   */
  <T> T factory(Class<?> objClass,
      Class<T> factClass, String factMethodName, Class<?>... argTypes)
  throws InstantiationRelatedException;

  //----------------------------------------------------------------------
  static class Metafactory implements Instantiation {

    public static Instantiation forClass(Class<?> klass)
    throws InstantiationRelatedException {
      //return new Metafactory();
      try {
        return (Instantiation) klass.getClassLoader()
          .loadClass(Metafactory.class.getName())
          .getConstructor()
          .newInstance();
      } catch (ClassNotFoundException | NoSuchMethodException
          | InstantiationException | IllegalAccessException
          | InvocationTargetException e) {
        throw new InstantiationRelatedException(
          "While creating Instantiation.Metafactory for class "
          + klass.getName() + ": " + e.getMessage(), e);
      }
    }

    /**
     * Dynamically construct a lambda to invoke a constructor.
     * The lambda will implement the method {@code factMethodName}
     * with arity {@code argTypes.length}
     * of functional interface {@code factClass}.
     * The emitted lambda is expected to be called with argument types
     * {@code argTypes},
     * and it is assumed (not checked) that those types can be assigned to
     * actual {@code factMethodName} arguments.
     *
     * <p>The lambda will call {@code objClass} constructor with arguments
     * {@code argTypes}.
     * There must exist a constructor with exactly that signature;
     * there is no search for a compatible constructor.
     *
     * <p>This method uses LambdaMetafactory; therefore, the returned lambda
     * should have a performance similar to a static {@code new} invocation
     * of the {@code objClass} constructor.
     * See {@link org.seismotech.laespe.InstanceBenchmark} for a benchmark
     * comparing several instantiation alternatives, including this one.
     *
     * @todo Add remaining check:
     * implemented {@code factClass} method accepts {@code argTypes}.
     * @todo Currently this method doesn't work for classes loaded with
     * HierarchySpecializer, because it uses the original class
     */
    @Override
    public <T> T factory(Class<?> objClass,
        Class<T> factClass, String factMethodName, Class<?>... argTypes)
    throws InstantiationRelatedException {
      final Method factMethod
        = findMethod(factClass, factMethodName, argTypes.length);
      final MethodType constType = methodType(void.class, argTypes);
      final MethodType declType = methodType(
        factMethod.getReturnType(), factMethod.getParameterTypes());
      final MethodType callType = methodType(objClass, argTypes);
      //Fails with *Invalid caller*
      //final MethodHandles.Lookup lookup = MethodHandles.lookup().in(objClass);
      final MethodHandles.Lookup lookup = MethodHandles.lookup();
      final MethodHandle constHandle;
      try {
        constHandle = lookup.findConstructor(objClass, constType);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new InstantiationRelatedException(
          "While locating constructor of " + objClass.getName()
          + " with signature " + constType + ": " + e.getMessage(), e);
      }
      final CallSite csite;
      try {
        csite = LambdaMetafactory.metafactory(
          lookup,
          factMethodName,
          methodType(factClass),
          declType,
          constHandle,
          callType);
      } catch (LambdaConversionException e) {
        throw new InstantiationRelatedException(
          "While creating a lambda metafactory for " + objClass.getName()
          + " implementing interace " + factClass.getName()
          + ", method " + factMethodName + "(" +Arrays.toString(argTypes)+ ")"
          + ": " + e.getMessage(), e);
      }
      final Object fact;
      try {
        fact = csite.getTarget().invoke();
      } catch (Throwable e) {
        throw new InstantiationRelatedException(
          "While creating a lambda for " + objClass.getName()
          + " implementing interace " + factClass.getName()
          + ", method " + factMethodName + "(" +Arrays.toString(argTypes)+ ")"
          + ": " + e.getMessage(), e);
      }
      return factClass.cast(fact);
    }

    private static Method findMethod(Class<?> klass, String name, int arity) {
      Method method = null;
      for (final Method cand: klass.getDeclaredMethods()) {
        if (cand.getParameterCount() != arity
            || !cand.getName().equals(name)) continue;
        if (method == null) {method = cand; continue;}
        throw new IllegalArgumentException(
          "Class `" + klass.getName() + "` has more than one `" + name + "`"
          + " method with arity " + arity + ": " + method + " and " + cand);
      }
      if (method == null) throw new IllegalArgumentException(
        "Cannot find a method named `" + name + "` with arity " + arity);
      return method;
    }
  }
}
