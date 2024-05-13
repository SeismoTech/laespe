package org.seismotech.laespe;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.*;

import org.seismotech.laespe.example.widehierarchy.*;

/**
 * @see AutoWideHierarchyBenchmark This is a previous, less flexible attempt.
 */
@Fork(value = 1)
@Warmup(iterations = 3, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ManualWideHierarchyBenchmark {

  @Param({"100"})
  int rows;
  @Param({"10000"})
  int cols;

  Random rnd;
  Seq[] dmat;
  Seq[] smat;

  @Setup
  public void doSetup()
  throws ClassNotFoundException, InstantiationRelatedException {
    final BoundedSpecializer hisp
      = new BoundedSpecializer(Seq.class, Instantiation.class);

    final Class<?> byteSeqClass = hisp.specialized(ByteSeq.class);
    final Class<?> shortSeqClass = hisp.specialized(ShortSeq.class);
    final Class<?> intSeqClass = hisp.specialized(IntSeq.class);
    final Class<?> longSeqClass = hisp.specialized(LongSeq.class);

    @SuppressWarnings("unchecked")
    final Function<byte[],Seq> byteSeqFact = Instantiation.fastFactory(
      byteSeqClass, Function.class, "apply", byte[].class);
    @SuppressWarnings("unchecked")
    final Function<short[],Seq> shortSeqFact = Instantiation.fastFactory(
      shortSeqClass, Function.class, "apply", short[].class);
    @SuppressWarnings("unchecked")
    final Function<int[],Seq> intSeqFact = Instantiation.fastFactory(
      intSeqClass, Function.class, "apply", int[].class);
    @SuppressWarnings("unchecked")
    final Function<long[],Seq> longSeqFact = Instantiation.fastFactory(
      longSeqClass, Function.class, "apply", long[].class);

    rnd = new Random();
    dmat = buildMatrix(ByteSeq::new, ShortSeq::new, IntSeq::new, LongSeq::new);
    smat = buildMatrix(byteSeqFact, shortSeqFact, intSeqFact, longSeqFact);
  }

  Seq[] buildMatrix(Function<byte[],Seq> byteSeq,
      Function<short[],Seq> shortSeq,
      Function<int[],Seq> intSeq,
      Function<long[],Seq> longSeq) {
    final Seq[] mat = new Seq[rows];
    for (int i = 0; i < rows; i++) {
      final int disc = rnd.nextInt(4);
      switch (disc) {
      case 0: mat[i] = bytes(cols, byteSeq); break;
      case 1: mat[i] = shorts(cols, shortSeq); break;
      case 2: mat[i] = ints(cols, intSeq); break;
      case 3: mat[i] = longs(cols, longSeq); break;
      }
    }
    return mat;
  }

  Seq bytes(int n, Function<byte[],Seq> fact) {
    final byte[] xs = new byte[n];
    for (int i = 0; i < n; i++) xs[i] = (byte) rnd.nextInt();
    return fact.apply(xs);
  }

  Seq shorts(int n, Function<short[],Seq> fact) {
    final short[] xs = new short[n];
    for (int i = 0; i < n; i++) xs[i] = (short) rnd.nextInt();
    return fact.apply(xs);
  }

  Seq ints(int n, Function<int[],Seq> fact) {
    final int[] xs = new int[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextInt();
    return fact.apply(xs);
  }

  Seq longs(int n, Function<long[],Seq> fact) {
    final long[] xs = new long[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextLong();
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
