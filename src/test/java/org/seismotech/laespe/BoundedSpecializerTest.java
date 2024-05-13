package org.seismotech.laespe;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.seismotech.laespe.example.widehierarchy.*;

class BoundedSpecializerTest {

  @Test
  void wideHierarchyTest()
  throws ClassNotFoundException {
    final BoundedSpecializer hisp
      = new BoundedSpecializer(Seq.class);

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
  throws ClassNotFoundException, InstantiationRelatedException {
    final BoundedSpecializer hisp1
      = new BoundedSpecializer(Seq.class);
    final Class<?> intSeqClass1 = hisp1.specialized(IntSeq.class);
    @SuppressWarnings("unchecked")
    final Function<int[],Seq> fact1 = Instantiation.fastFactory(
      intSeqClass1, Function.class, "apply", int[].class);
    final Seq xs1 = fact1.apply(new int[] {0, 1, 2, 3, 4});
    assertFalse(intSeqClass1.isInstance(xs1));
    assertTrue(xs1 instanceof IntSeq);

    final BoundedSpecializer hisp2
      = new BoundedSpecializer(Instantiation.class, Seq.class);
    final Class<?> intSeqClass2 = hisp2.specialized(IntSeq.class);
    @SuppressWarnings("unchecked")
    final Function<int[],Seq> fact2 = Instantiation.fastFactory(
      intSeqClass2, Function.class, "apply", int[].class);
    final Seq xs2 = fact2.apply(new int[] {0, 1, 2, 3, 4});
    assertTrue(intSeqClass2.isInstance(xs2));
  }
}
