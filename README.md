LaEspe: Code reloading for class specialization
======================================================================

This is an small package to simplify multiple loading of a class hierarchy
to get code specialized for specific types.

Let's see a few examples.

### Wide hierarchy
Suppose we have a class hierarchy based on interface `I`,
an intermediate abstract class `A` partially implementing `I`
and multiple implementation classes `C1`, ..., `Cn` extending `A`.
There is a method `I::m` implemented in `A`
that *heavily* uses other `I` methods not implemented in `A`.
By *heavily* we mean, for instance, that those site calls are nested
inside a loop.
We are going to call that megamorphic call sites
*hard megamorphic* or having *hard megamorphims*.

Suppose that we have a call site X somewhere calling `I::m` that is highly 
polymorphic.
`A::m` will be probably inlined at X,
but the inlined code will suffer the same amount of polymorphims.
Therefore, all calls in `A::m` to other methods of `I` will be
megamorphic and the JVM will not inline them.
Because those calls are *hard*, the performance will be very bad.

Suppose now that we force isolated loads for every `Ci`,
each one loading a clean copy of `A`.
Let's call `Ai` those copies of `A`.
The interface `I` is shared across all those isolated loads,
otherwise we would not have a way to type and call `Ci` instances.
Let's reconsider that call site X.
It is still megamorphic, but it is *directly* megamorphic:
the JVM is seeing multiple implementations of `I::m`,
one different implementation for each `Ai`.
Therefore is not going to inline any `Ai::m`
and will keep that call as a virtual call.
On the other side, call sites in `Ai::m` to other `I` methods will became
monomorphic, because that `Ai` is attached to `Ci` only, and will probably
became inlined.
The resulting compiled code has 1 virtual call to `I::m`
but many inner monomorphic (even inlined) calls.
The performance will be much better.


The name of the game
----------------------------------------------------------------------

Because this is a small but very powerful specialization package,
we named it after a small but powerful spanish superhero,
almost imposible to defeat: *La Espe*.
