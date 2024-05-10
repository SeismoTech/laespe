package org.seismotech.laespe;

import java.lang.reflect.Constructor;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.*;

import org.seismotech.laespe.example.widehierarchy.*;

/**
 * Hotspot jdk21
Benchmark                                  (cols)  (poly)  (rows)   Mode  Cnt     Score   Error  Units
.sumAllDirect        10000       1     100  thrpt    2  5147.475          ops/s
.sumAllDirect        10000       2     100  thrpt    2  5465.069          ops/s
.sumAllDirect        10000       3     100  thrpt    2   189.397          ops/s
.sumAllDirect        10000       6     100  thrpt    2   187.632          ops/s
.sumAllDirect        10000      10     100  thrpt    2   187.245          ops/s
.sumAllSpecialized   10000       1     100  thrpt    2  5148.003          ops/s
.sumAllSpecialized   10000       2     100  thrpt    2  5656.643          ops/s
.sumAllSpecialized   10000       3     100  thrpt    2  5433.281          ops/s
.sumAllSpecialized   10000       6     100  thrpt    2  5526.483          ops/s
.sumAllSpecialized   10000      10     100  thrpt    2  5488.516          ops/s
 *
 * Graal EE jdk21
Benchmark                                  (cols)  (poly)  (rows)   Mode  Cnt     Score   Error  Units
.sumAllDirect        10000       1     100  thrpt    2  4273.908          ops/s
.sumAllDirect        10000       2     100  thrpt    2  4243.973          ops/s
.sumAllDirect        10000       3     100  thrpt    2  4231.116          ops/s
.sumAllDirect        10000       6     100  thrpt    2   939.546          ops/s
.sumAllDirect        10000      10     100  thrpt    2   534.363          ops/s
.sumAllSpecialized   10000       1     100  thrpt    2  4314.082          ops/s
.sumAllSpecialized   10000       2     100  thrpt    2  4204.770          ops/s
.sumAllSpecialized   10000       3     100  thrpt    2  4177.823          ops/s
.sumAllSpecialized   10000       6     100  thrpt    2  4235.292          ops/s
.sumAllSpecialized   10000      10     100  thrpt    2  4167.744          ops/s
 *
 * Zing jdk21:
Benchmark                                  (cols)  (poly)  (rows)   Mode  Cnt     Score   Error  Units
.sumAllDirect        10000       1     100  thrpt    2  5799.535          ops/s
.sumAllDirect        10000       2     100  thrpt    2  5614.979          ops/s
.sumAllDirect        10000       3     100  thrpt    2  5820.650          ops/s
.sumAllDirect        10000       6     100  thrpt    2  5719.773          ops/s
.sumAllDirect        10000      10     100  thrpt    2   563.973          ops/s
.sumAllSpecialized   10000       1     100  thrpt    2  5334.770          ops/s
.sumAllSpecialized   10000       2     100  thrpt    2  5324.161          ops/s
.sumAllSpecialized   10000       3     100  thrpt    2  5358.895          ops/s
.sumAllSpecialized   10000       6     100  thrpt    2  6058.967          ops/s
.sumAllSpecialized   10000      10     100  thrpt    2  5521.901          ops/s

 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AutoWideHierarchyBenchmark {

  @Param({"1", "2", "3", "6", "10"})
  int poly;
  @Param({"100"})
  int rows;
  @Param({"10000"})
  int cols;

  Random rnd;
  Seq[] dmat;
  Seq[] smat;

  @Setup
  public void doSetup()
  throws Throwable {
    final HierarchySpecializer hisp
      = new HierarchySpecializer(Seq.class, Instantiation.class);
    final HierarchySpecializer hispAbs
      = new HierarchySpecializer(AbsSeq.class, Instantiation.class);

    @SuppressWarnings("unchecked")
    final Function<int[],Seq>[] sharedFacts = new Function[poly];
    @SuppressWarnings("unchecked")
    final Function<int[],Seq>[] specFacts = new Function[poly];
    for (int i = 0; i < poly; i++) {
      @SuppressWarnings("unchecked")
      final Function<int[],Seq> shared = Instantiation.fastFactory(
        hispAbs.specialized(IntSeq.class),
        Function.class, "apply", int[].class);
      @SuppressWarnings("unchecked")
      final Function<int[],Seq> spec = Instantiation.fastFactory(
        hisp.specialized(IntSeq.class),
        Function.class, "apply", int[].class);
      sharedFacts[i] = shared;
      specFacts[i] = spec;
    }

    rnd = new Random();
    dmat = buildMatrix(sharedFacts);
    smat = buildMatrix(specFacts);
  }

  Seq[] buildMatrix(Function<int[],Seq>[] facts) {
    final Seq[] mat = new Seq[rows];
    for (int i = 0; i < rows; i++) {
      final int disc = rnd.nextInt(facts.length);
      mat[i] = ints(cols, facts[disc]);
    }
    return mat;
  }

  Seq ints(int n, Function<int[],Seq> fact) {
    final int[] xs = new int[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextInt();
    return fact.apply(xs);
  }

  //----------------------------------------------------------------------
  @Benchmark
  public long sumAllDirect() {
    return sumAll(dmat);
  }

  @Benchmark
  public long sumAllSpecialized() {
    return sumAll(smat);
  }

  private static long sumAll(Seq[] mat) {
    long s = 0;
    for (int i = 0; i < mat.length; i++) s += mat[i].sum();
    return s;
  }
}
