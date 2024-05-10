import java.util.Random;
import java.util.function.Function;

public class Main {

  static final Random rnd = new Random();

  public static void main(String[] args) {
    final int ROUNDS = Integer.parseInt(args[0]);
    final int ROWS = Integer.parseInt(args[1]);
    final int COLS = Integer.parseInt(args[2]);
    doIt(ROUNDS, ROWS, COLS);
  }

  static void doIt(int rounds, int rows, int cols) {
    final Seq[] mat = buildMatrix(rows, cols);
    long s = 0;
    for (int i = 0; i < rounds; i++) {
      s += sumAll(mat);
    }
    System.err.println(s);
  }

  static Seq[] buildMatrix(int rows, int cols) {
    final Seq[] mat = new Seq[rows];
    for (int i = 0; i < rows; i++) {
      final int disc = rnd.nextInt(6);
      switch (disc) {
      case 0: mat[i] = shorts(cols); break;
      case 1: mat[i] = ints(cols, IntSeq::new); break;
      case 2: mat[i] = longs(cols); break;
      case 3: mat[i] = ints(cols, IntSeq1::new); break;
      case 4: mat[i] = ints(cols, IntSeq2::new); break;
      case 5: mat[i] = ints(cols, IntSeq3::new); break;
      }
    }
    return mat;
  }

  static ShortSeq shorts(int n) {
    final short[] xs = new short[n];
    for (int i = 0; i < n; i++) xs[i] = (short) rnd.nextInt();
    return new ShortSeq(xs);
  }

  static Seq ints(int n, Function<int[],Seq> fact) {
    final int[] xs = new int[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextInt();
    return fact.apply(xs);
  }

  static LongSeq longs(int n) {
    final long[] xs = new long[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextLong();
    return new LongSeq(xs);
  }

  static long sumAll(Seq[] mat) {
    long s = 0;
    for (int i = 0; i < mat.length; i++) s += mat[i].sum();
    return s;
  }
}
