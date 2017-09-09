# faster-multimethods [![Clojars Project](https://img.shields.io/clojars/v/palisades-lakes/faster-multimethods.svg)](https://clojars.org/palisades-lakes/faster-multimethods)

Backwards compatible alternative to the 
Clojure 1.8.0 implementation of generic functions (aka multimethods)
via  `defmulti`/`defmethod`/`MultiFn`.

Very roughly 1/10 the cost for method lookup of Clojure 1.8.0,
and comparable in performance to `defprotocol` while being
fully dynamic,
See 
[multimethod-experiments](https://github.com/palisades-lakes/multimethod-experiments)
for benchmark details.

Runtimes for various dynamic method lookup algorithms. 
`hashmaps`, `signatures`, and `nohierarchy` are available from
`faster-multimethods`.

![faster-multimethods vs Clojure 1.8.0](docs/figs/dynamic-multi.quantiles.png)

Overhead, taking a hand optimized Java if-then-else `instanceof`
algorithm as the baseline, as a fraction of the overhead of
Clojure 1.8.0 `defmulti`:

![faster-multimethods vs Clojure 1.8.0](docs/figs/dynamic-multi-overhead.quantiles.png)

## Changes from Clojure 1.8.0

The main differences from the Clojure 1.8.0 implementation:

1. In
[`MultiFn`](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/MultiFn.java),
replace 
[`PersistentHashMap`](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/PersistentHashMap.java)
with 
[`java.util.HashMap`](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html)
(`hashmaps` in the plots).

    The HashMaps here are treated as immutable, so their behavior
is functionally equivalent. 
However, this requires a fair amount of discipline from all
future developer, so is perhaps not the best choice.
Experiments with immutable
maps from [Guava](https://github.com/google/guava) showed 
essentially the same level of performance, but I prefer not to add
such a large dependency to this tiny bit of code.
I could include a minimal immutable map implementation, but that 
would be duplicating work better done elsewhere.

    The fact that HashMaps perform so much better that PersistentHashMaps,
in this context, suggests that there might be opportunities for
improving the performance of Clojure collections more generally.
Supporting evidence can be seen in
[clj-tuple](https://github.com/ztellman/clj-tuple)
and 
[cambrian-collections](https://github.com/ztellman/cambrian-collections).
See also:
[Z. Tellman, Using Clojure To Generate Java To Reimplement Clojure](https://www.factual.com/blog/using-clojure-to-generate-java-to-reimplement-clojure). 
It is possible that performance improvements to the
Clojure collections would make this little library unnecessary.

2. Permit more efficient dispatch values (`signatures` in the plots).

    I've chosen to add support for an additional special case of
    dispatch values,
    which I call Signatures --- essentially short lists of Classes.
    
    This is backwards compatible; and can be adopted by changing
    the dispatch function from, eg, `[(class a) (class b)]` to
    `(signature a b)`.
    
4. Permit a `:hierarchy false` option to `defmulti`
(`nohierarchy` in the plots).

    Every multimethod (instance of MultiFn) contains a reference
    to a `Var` holding a `hierarchy`. This shared mutable state must
    be checked for changes every time a multimethod is called.
    
    An important special case is one where only classes,
    and sequences of classes
    are used as dispatch values. In this case, the hierarchy
    is irrelevant. 
    
    Removing the need for synchronizing with the `hierarchy`,
    further reduces the overhead.
    
## Usage

### Dependency 

Maven:

```xml
<dependency>
  <groupId>palisades-lakes</groupId>
  <artifactId>faster-multimethods</artifactId>
  <version>0.0.3</version>
</dependency>
```

Leinigen/Boot:
```clojure
Leiningen/Boot
[palisades-lakes/faster-multimethods "0.0.3"]
```

### Code examples

Fastest:

```
(require `[palisades.lakes.multimethods.core :as fmc])

(fmc/defmulti intersects?
  "Test for general set intersection."
  
  {}  
  
  (fn intersects?-dispatch [s0 s1] (fmc/extract-signature s0 s1))
  
  :hierarchy false)
  
(fmc/defmethod intersects? 
  (fmc/signature IntegerInterval java.util.Set)
  [^IntegerInterval s0 ^java.util.Set s1]
  (.intersects s0 s1))
  
 ...
 ``` 
  
Most general:

```
(require `[palisades.lakes.multimethods.core :as fmc])

(fmc/defmulti intersects?
  "Test for general set intersection."
  
  {}  
  
  (fn intersects?-dispatch [s0 s1] [(class s0) (class s1))))
  
(fmc/defmethod intersects? 
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





