package org.seismotech.laespe;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.seismotech.laespe.example.widehierarchy.*;

class HierarchySpecializerTest {

  @Test
  void wideHierarchyTest()
  throws Exception {
    final HierarchySpecializer hisp
      = new HierarchySpecializer(Seq.class);

    assertEquals(AbsSeq.class, IntSeq.class.getSuperclass());

    final Class<?> intSeqClass = hisp.specialized(IntSeq.class);
    assertEquals(IntSeq.class.getName(), intSeqClass.getName());
    assertNotEquals(IntSeq.class, intSeqClass);
    assertNotEquals(AbsSeq.class, intSeqClass.getSuperclass());
    assertTrue(Seq.class.isAssignableFrom(intSeqClass));
    assertFalse(AbsSeq.class.isAssignableFrom(intSeqClass));

    final Class<?> longSeqClass = hisp.specialized(LongSeq.class);
    assertEquals(LongSeq.class.getName(), longSeqClass.getName());
    assertNotEquals(LongSeq.class, longSeqClass);
    assertNotEquals(AbsSeq.class, longSeqClass.getSuperclass());
    assertTrue(Seq.class.isAssignableFrom(longSeqClass));
    assertFalse(AbsSeq.class.isAssignableFrom(longSeqClass));

    assertNotEquals(intSeqClass.getSuperclass(), longSeqClass.getSuperclass());
  }

  @Test
  void wideHierarchyFactoryTest()
  throws Throwable {
    final HierarchySpecializer hisp1
      = new HierarchySpecializer(Seq.class);
    final Class<?> intSeqClass1 = hisp1.specialized(IntSeq.class);
    @SuppressWarnings("unchecked")
    final Function<int[],Seq> fact1 = Instantiation.fastFactory(
      intSeqClass1, Function.class, "apply", int[].class);
    final Seq xs1 = fact1.apply(new int[] {0, 1, 2, 3, 4});
    assertFalse(intSeqClass1.isInstance(xs1));
    assertTrue(xs1 instanceof IntSeq);

    final HierarchySpecializer hisp2
      = new HierarchySpecializer(Instantiation.class, Seq.class);
    final Class<?> intSeqClass2 = hisp2.specialized(IntSeq.class);
    @SuppressWarnings("unchecked")
    final Function<int[],Seq> fact2 = Instantiation.fastFactory(
      intSeqClass2, Function.class, "apply", int[].class);
    final Seq xs2 = fact2.apply(new int[] {0, 1, 2, 3, 4});
    assertTrue(intSeqClass2.isInstance(xs2));
  }
}
