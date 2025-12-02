package org.seismotech.laespe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.StringConcatFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.*;

/**
 * Intel(R) Core(TM) i9-9880H CPU @ 2.30GHz
 * Linux, turbo disabled.
 *
 * JDK 23
builder        thrpt    2  23986723.876          ops/s
plus           thrpt    2  25349914.019          ops/s
scfHandle      thrpt    2  26472830.314          ops/s
scfLambda      thrpt    2  25907471.154          ops/s
scfMetaLambda  thrpt    2  24976953.772          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StringConcatBenchmark {

  public static class MyObject {
    final int x;
    public MyObject(int x) {
      this.x = x;
    }
  }

  public static interface Messager {
    String message(int x, String u, int y, String w);
  }

  int x = 10;
  int y = 11;
  String u = " + ";
  String w = "i";

  Messager plusLambda;
  Messager builderLambda;
  MethodHandle scfHandle;
  Messager scfLambda;
  Messager scfMetaLambda;

  @Setup
  public void doSetup()
  throws Throwable {
    plusLambda = (x, u, y, w) -> x + u + y + w;
    builderLambda = (x, u, y, w) ->
      new StringBuilder().append(x).append(u).append(y).append(w).toString();

    final MethodType msgType = MethodType.methodType(
      String.class, int.class, String.class, int.class, String.class);
    scfHandle = StringConcatFactory.makeConcat(
      MethodHandles.lookup(), "concat", msgType).getTarget();

    scfLambda = (x, u, y, w) -> {
      try {
        return (String) scfHandle.invokeExact(x, u, y, w);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    };

    // Fails with
    // java.lang.invoke.LambdaConversionException: MethodHandle(int,String,int,String)String is not direct or cannot be cracked
    // scfMetaLambda = (Messager)
    //   LambdaMetafactory.metafactory(
    //     MethodHandles.lookup(),
    //     "message",
    //     MethodType.methodType(Messager.class),
    //     msgType,
    //     scfHandle,
    //     msgType)
    //   .getTarget().invokeExact();

    // Trick to avoid previous error.
    // https://stackoverflow.com/questions/77098680/methodhandle-cannot-be-cracked-when-using-lambdametafactory
    // Not sure this trick produce the same *intended* code (performance)
    // that the previous failed attempt in any circunstance.
    // See InstanceBenchmark for an example where this approach has
    // a much worse performance (dynLambda vs dynMetaLambda).
    scfMetaLambda = (Messager)
      LambdaMetafactory.metafactory(
        MethodHandles.lookup(),
        "message",
        MethodType.methodType(Messager.class, MethodHandle.class),
        msgType,
        MethodHandles.exactInvoker(msgType),
        msgType)
      .getTarget().invokeExact(scfHandle);

  }

  @Benchmark
  public Object plus() {
    return plusLambda.message(x, u, y, w);
  }

  @Benchmark
  public Object builder() {
    return builderLambda.message(x, u, y, w);
  }

  @Benchmark
  public Object scfLambda() {
    return scfLambda.message(x, u, y, w);
  }

  @Benchmark
  public Object scfMetaLambda() {
    return scfMetaLambda.message(x, u, y, w);
  }

  @Benchmark
  public Object scfHandle() throws Throwable {
    return (String) scfHandle.invokeExact(x, u, y, w);
  }
}
