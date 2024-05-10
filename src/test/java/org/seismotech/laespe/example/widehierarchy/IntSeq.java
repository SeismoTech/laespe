package org.seismotech.laespe.example.widehierarchy;

public class IntSeq extends AbsSeq implements Seq {
  private final int[] xs;

  public IntSeq(int[] xs) {this.xs = xs;}

  @Override public int length() {return xs.length;}
  @Override public long get(int i) {return xs[i];}

  // @Override
  // public long sum() {
  //   final int n = length();
  //   long s = 0;
  //   for (int i = 0; i < n; i++) s += get(i);
  //   return s;
  // }
}
