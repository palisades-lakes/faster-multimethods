# overview

[![Clojars Project](https://img.shields.io/clojars/v/palisades-lakes/faster-multimethods.svg)](https://clojars.org/palisades-lakes/faster-multimethods)

Alternative to the 
Clojure 1.8.0 implementation of generic functions (aka multimethods)
via  `defmulti`/`defmethod`/`MultiFn`.

Very roughly 1/10 the cost for method lookup of Clojure 1.8.0,
and comparable in performance to using protocols, while being
fully dynamic.

## benchmarks

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

## Usage

### Dependency 

#### Maven:

```xml
<dependency>
  <groupId>palisades-lakes</groupId>
  <artifactId>faster-multimethods</artifactId>
  <version>0.1.0</version>
</dependency>
```

#### Leiningen/Boot:
```clojure
[palisades-lakes/faster-multimethods "0.1.0"]
```

### Code examples

#### Fastest:

```clojure
(require `[palisades.lakes.multimethods.core :as plm])

(plm/defmulti intersects?
  "Test for general set intersection."
  {}  
  (fn intersects?-dispatch [s0 s1] (plm/extract-signature s0 s1))
  :hierarchy false)
  
(plm/defmethod intersects? 
  (plm/signature IntegerInterval java.util.Set)
  [^IntegerInterval s0 ^java.util.Set s1]
  (.intersects s0 s1))
  
...
```

#### Most general:

```clojure
(require `[palisades.lakes.multimethods.core :as plm])

(plm/defmulti intersects?
  "Test for general set intersection."
  {}  
  (fn intersects?-dispatch [s0 s1] [(class s0) (class s1))))

(plm/defmethod intersects? 
  [IntegerInterval java.util.Set]
  [^IntegerInterval s0 ^java.util.Set s1]
  (some #(.contains s0 %) s1))
  
...
```

## Acknowledgments

### ![Yourkit](https://www.yourkit.com/images/yklogo.png)

YourKit is kindly supporting open source projects with its full-featured Java
Profiler.

YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

* <a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
* <a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.





