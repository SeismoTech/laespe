Wide hierarchy use-case
======================================================================

This is a sketch of a wide hierarchy example to check the impact of 
inner loops having megamorphic (virtual) call sites.

Interface `Seq` is the root.
Abstract class `AbsSeq` implements a hard megamorphic method `sum()`
because it has a megamorphic call site to a small method `get(int)`
inside a loop.
Leaf implementations `ShortSeq`, `IntSeq` and `LongSeq`
implement `length()` and `get(int)`,
and have a commented out copy of `sum()`.

`Main` has a *conceptually* megamorphic call site to `sum()`,
because it is applied to all `Seq` implementations.
But when `AbsSeq::sum` is the only `sum()`, that call site is calling always
the same method, therefore it is monomorphic
and `AbsSeq::sum` is inlined in `Main`.
But `AbsSeq::sum` has a call site to `get(int)`
that becames megamorphic and therefore cannot be inlined.
The end result has a pretty bad performance.

After uncommenting `sum()` copies in `ShortSeq`, ...,
the `Main` call to `sum()` becames megamorphic, `sum()` is not inlined,
but all call sites to `get(int)` inside each `sum()` are monomorphic 
and are inlined.
The net result is much better performance, around 2 orders of magnitude.

The main difference between those 2 scenarios is whether the megamorphic
call site is inside or outside internal loops.

This is a perfect example for `AbsSeq` specialization.
