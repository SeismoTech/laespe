package org.seismotech.laespe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.openjdk.jmh.annotations.*;

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
  public Object dynLambda() {
    return ((IntFunction<MyObject>) dynLambda).apply(x);
  }

  @Benchmark
  @SuppressWarnings("unchecked")
  public Object dynSpookyLambda() {
    return ((IntFunction<MyObject>) dynLambda).apply(x);
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
