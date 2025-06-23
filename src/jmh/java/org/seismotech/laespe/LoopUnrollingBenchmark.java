package org.seismotech.laespe;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.*;

import org.seismotech.laespe.example.widehierarchy.*;

/**
 * A benchmark to measure a poor man full loop unrolling with class
 * specialization.
 *
 * <p>A typical use case is an array of objects from a wide hierarchy
 * that must be processed calling some method M.
 * The obvious approach, writting a loop,
 * will create a megamorphic call site to M.
 *
 * <p>Another approach is to do a recursive walk of the array.
 * That is not going to be better.
 * Except if we create a new class for each step and link them so that
 * the recursive call is, in fact, a call to a copy of itself in another
 * class.
 * We will call *forward calls* to these pseudo-recursive calls;
 * forward calls are recursive calls to the same method in another
 * specialization of the class.
 * A good compiler will discover that all those forward calls are monomorphic
 * and inlineable.
 *
 * <p>This approach is sensible even for long arrays.
 * Probably, the compiller will reach some inlining limit and will not
 * inline all the calls in the initial call site.
 * But the first forward call that is not inline will become a new place
 * to inline the next forward calls.
 * In the end, we expect the compiler to transform N forward calls in
 * N/M monomorphic calls, each containing M inlined forward calls.
 *
 * Hotspot jdk21
Benchmark        (length)   Mode  Cnt          Score   Error  Units
.fixedExecution         1  thrpt    2  407836244.045          ops/s
.fixedExecution         2  thrpt    2  278277711.097          ops/s
.fixedExecution         3  thrpt    2  210148507.206          ops/s
.fixedExecution         6  thrpt    2  115541161.061          ops/s
.fixedExecution        10  thrpt    2   68841626.912          ops/s
.fixedExecution       100  thrpt    2    3676767.409          ops/s
.loopExecution          1  thrpt    2  273821323.564          ops/s
.loopExecution          2  thrpt    2  173304934.359          ops/s
.loopExecution          3  thrpt    2   46420616.612          ops/s
.loopExecution          6  thrpt    2   25454609.561          ops/s
.loopExecution         10  thrpt    2   16335961.802          ops/s
.loopExecution        100  thrpt    2     818388.126          ops/s
 *
 * Graal EE jdk21
.fixedExecution         1  thrpt    2  349822012.124          ops/s
.fixedExecution         2  thrpt    2  251728843.755          ops/s
.fixedExecution         3  thrpt    2  197192520.727          ops/s
.fixedExecution         6  thrpt    2  106945576.446          ops/s
.fixedExecution        10  thrpt    2   61566101.995          ops/s
.fixedExecution       100  thrpt    2    1016913.848          ops/s
.loopExecution          1  thrpt    2  567318665.122          ops/s
.loopExecution          2  thrpt    2  277085598.914          ops/s
.loopExecution          3  thrpt    2  161133276.964          ops/s
.loopExecution          6  thrpt    2   34764951.790          ops/s
.loopExecution         10  thrpt    2   16461947.833          ops/s
.loopExecution        100  thrpt    2     658355.533          ops/s
 *
 * Zing jdk21:
.fixedExecution         1  thrpt    2  255390304.775          ops/s
.fixedExecution         2  thrpt    2  180433522.644          ops/s
.fixedExecution         3  thrpt    2  187549157.003          ops/s
.fixedExecution         6  thrpt    2  116563395.826          ops/s
.fixedExecution        10  thrpt    2   72186924.501          ops/s
.fixedExecution       100  thrpt    2     108760.449          ops/s
.loopExecution          1  thrpt    2  402985573.358          ops/s
.loopExecution          2  thrpt    2  307350675.461          ops/s
.loopExecution          3  thrpt    2  244657216.677          ops/s
.loopExecution          6  thrpt    2   55070834.342          ops/s
.loopExecution         10  thrpt    2   17457452.605          ops/s
.loopExecution        100  thrpt    2    1076708.154          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class LoopUnrollingBenchmark {

  @Param({"1", "2", "3", "6", "10", "100"})
  int length;

  StateAction[] actions;
  ArrayExecutor fixedExecutor;

  @Setup
  public void doSetup()
  throws Exception {
    final BoundedSpecializer hisp = new BoundedSpecializer(
      Instantiation.class,
      StateAction.class, ArrayExecutor.class);

    actions = new StateAction[length];
    for (int i = 0; i < length; i++) {
      actions[i] = (StateAction) hisp
        .specialized(Incr.class)
        .getConstructor()
        .newInstance();
    }

    fixedExecutor = new EmptyArrayExecutor();
    for (int i = 0; i < length; i++) {
      fixedExecutor = (ArrayExecutor) hisp
        .specialized(LinkedArrayExecutor.class)
        .getConstructor(ArrayExecutor.class)
        .newInstance(fixedExecutor);
    }
  }

  public static class State {
    public int counter = 0;
  }

  public static interface StateAction {
    void perform(State state);
  }

  public static class Incr implements StateAction {
    @Override public void perform(State state) {state.counter++;}
  }

  public static interface ArrayExecutor {
    void perform(State state, StateAction[] as, int i);
  }

  public static class EmptyArrayExecutor implements ArrayExecutor {
    @Override public void perform(State state, StateAction[] as, int i) {}
  }

  public static class LinkedArrayExecutor implements ArrayExecutor {
    private final ArrayExecutor next;

    public LinkedArrayExecutor(ArrayExecutor next) {this.next = next;}

    @Override public void perform(State state, StateAction[] as, int i) {
      as[i].perform(state);
      next.perform(state, as, i+1);
    }
  }

  //----------------------------------------------------------------------
  @Benchmark
  public int loopExecution() {
    final State state = new State();
    for (final StateAction action: actions) action.perform(state);
    return state.counter;
  }

  @Benchmark
  public int fixedExecution() {
    final State state = new State();
    fixedExecutor.perform(state, actions, 0);
    return state.counter;
  }
}
