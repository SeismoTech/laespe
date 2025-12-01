package org.seismotech.laespe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandleProxies;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.openjdk.jmh.annotations.*;

/**
 * Intel(R) Core(TM) i9-9880H CPU @ 2.30GHz
 * Linux, turbo disabled.
 *
 * JDK 23
_new                    thrpt    2  291285772.987          ops/s
constructorNewInstance  thrpt    2   68648468.351          ops/s
dynBoxedLambda          thrpt    2  260390319.320          ops/s
dynLambda               thrpt    2  281405936.692          ops/s
dynSpookyLambda         thrpt    2  282433484.206          ops/s
handleInvoke            thrpt    2  114628766.520          ops/s
handleInvokeExact       thrpt    2  125449350.854          ops/s
instLambda              thrpt    2  288471051.641          ops/s
lambda                  thrpt    2  288239074.938          ops/s
proxyLambda             thrpt    2  116778350.705          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class InstanceBenchmark {

  public static class MyObject {
    final int x;
    public MyObject(int x) {
      this.x = x;
    }
  }

  int x = 0;
  IntFunction<MyObject> lambda;
  Constructor<MyObject> constructor;
  MethodHandle constHandle;
  IntFunction<MyObject> proxyLambda;
  IntFunction<MyObject> dynLambda;
  Object dynSpookyLambda;
  Function<Integer,MyObject> dynBoxedLambda;
  IntFunction<MyObject> instLambda;

  @Setup
  public void doSetup()
  throws Throwable {
    lambda = MyObject::new;
    constructor = MyObject.class.getDeclaredConstructor(int.class);

    final MethodType constType = MethodType.methodType(void.class, int.class);
    final MethodType applyType = MethodType.methodType(MyObject.class, int.class);
    constHandle
      = MethodHandles.publicLookup().findConstructor(MyObject.class, constType);

    @SuppressWarnings("unchecked")
    final IntFunction<MyObject> proxy = (IntFunction<MyObject>)
      MethodHandleProxies.asInterfaceInstance(IntFunction.class, constHandle);
    proxyLambda = proxy;

    dynLambda = (IntFunction<MyObject>)
      LambdaMetafactory.metafactory(
        MethodHandles.lookup(),
        "apply",
        MethodType.methodType(IntFunction.class),
        MethodType.methodType(Object.class, int.class),
        constHandle,
        applyType)
      .getTarget().invokeExact();
    dynSpookyLambda =
      LambdaMetafactory.metafactory(
        MethodHandles.lookup(),
        "apply",
        MethodType.methodType(IntFunction.class),
        MethodType.methodType(Object.class, int.class),
        constHandle,
        applyType)
      .getTarget().invoke();
    dynBoxedLambda = (Function<Integer,MyObject>)
      LambdaMetafactory.metafactory(
        MethodHandles.lookup(),
        "apply",
        MethodType.methodType(Function.class),
        MethodType.methodType(Object.class, Object.class),
        constHandle,
        MethodType.methodType(MyObject.class, Integer.class))
      .getTarget().invokeExact();

    @SuppressWarnings("unchecked")
    final IntFunction<MyObject> inst = Instantiation.fastFactory(
      MyObject.class, IntFunction.class, "apply", int.class);
    instLambda = inst;
  }

  @Benchmark
  public Object _new() {
    return new MyObject(x);
  }

  @Benchmark
  public Object lambda() {
    return lambda.apply(x);
  }

  @Benchmark
  public Object proxyLambda() {
    return proxyLambda.apply(x);
  }

  @Benchmark
  public Object dynLambda() {
    return dynLambda.apply(x);
  }

  @Benchmark
  @SuppressWarnings("unchecked")
  public Object dynSpookyLambda() {
    return ((IntFunction<MyObject>) dynSpookyLambda).apply(x);
  }

  @Benchmark
  public Object dynBoxedLambda() {
    return dynBoxedLambda.apply(x);
  }

  @Benchmark
  public Object instLambda() {
    return instLambda.apply(x);
  }

  @Benchmark
  public Object constructorNewInstance()
  throws Exception {
    return constructor.newInstance(x);
  }

  @Benchmark
  public Object handleInvoke()
  throws Throwable {
    return constHandle.invoke(x);
  }

  @Benchmark
  public Object handleInvokeExact()
  throws Throwable {
    return (MyObject) constHandle.invokeExact(x);
  }
}
