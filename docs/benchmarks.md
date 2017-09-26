# benchmarks

See 
[multimethod-experiments](https://github.com/palisades-lakes/multimethod-experiments)
for details.

Runtimes for various dynamic method lookup algorithms:

- `hashmaps` (faster-multimethods): Clojure 1.8.0 behavior except 
for 'bug' fixes. Replaces persistent hashmaps with Java hashmaps.
(See [changes](changes.html) for details.)
 
- `signatures`(faster-multimethods): 
uses specialized `Signature` dispatch values in place of persistent vectors.

- `nohierarchy`(faster-multimethods): optimizes for pure class-based dispatch.

- `defmulti`: Clojure 1.8.0

- `protocols`:  Clojure 1.8.0 `defprotocol`,
with hand-optimized if-then-else `instance?` calls to look up the 
correct method based on all arguments.

- `instanceof` hand optimized if-then-else Java method lookup;

- `instancefn` same as `instanceof` but implemented in Clojure and
invoking Clojure functions
rather than Java methods.

- `dynafun`: an experimental, incomplete library abandoning 
consistency with Clojure 1.8.0. No hierarchies; 
pure class-based method definition
and lookup.
For small arities, the current version does linear search in 
nested arrays, and avoids allocating and reclaiming dispatch values.

<img
src="../figs/dynamic-multi.quantiles.png"
alt="faster-multimethods vs Clojure 1.8.0 runtimes"
style="width: 24cm">

Overhead, taking a hand optimized Java if-then-else `instanceof`
algorithm as the baseline, as a fraction of the overhead of
Clojure 1.8.0 `defmulti`:

<img
src="../figs/dynamic-multi-overhead.quantiles.png"
alt="faster-multimethods overhead as a fraction of Clojure 1.8.0"
style="width: 24cm">

Note that [faster-multimethods](https://github.com/palisades-lakes/faster-multimethods)
outperforms Clojure 1.8.0 protocols except in the case of repeated calls
to a single method (the lowest curve in each plot).
When restricted to pure class-based dispatch (`nohierarchy`),
[faster-multimethods](https://github.com/palisades-lakes/faster-multimethods)
is close even in the single repeated method case,
while being fully dynamic (unlike protocols, which are only dynamic
for the first `this` argument).

I don't understand why `protocols` so much worse than everything 
else for the `diameter` benchmark. That is a single argument 
multimethod (`this` only) and ought to be an easy case for protocols.

A caveat: These benchmarks are measured after a lot of warmup,
giving HotSpot plenty of time to optimize what it can. 
Results might be very different in scenarios where the methods
are not called as often.

