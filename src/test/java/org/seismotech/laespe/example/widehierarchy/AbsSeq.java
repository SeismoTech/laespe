package org.seismotech.laespe.example.widehierarchy;

public abstract class AbsSeq implements Seq {
  @Override
  public long sum() {
    final int n = length();
    long s = 0;
    for (int i = 0; i < n; i++) s += get(i);
    return s;
  }
}
