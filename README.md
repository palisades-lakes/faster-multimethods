# faster-multimethods 

[![Clojars Project](https://img.shields.io/clojars/v/palisades-lakes/faster-multimethods.svg)](https://clojars.org/palisades-lakes/faster-multimethods)

Alternative to the 
Clojure 1.8.0 implementation of generic functions (aka multimethods)
via  `defmulti`/`defmethod`/`MultiFn`.

Very roughly 1/10 the cost for method lookup of Clojure 1.8.0,
and comparable in performance to using protocols, while being
fully dynamic.

Brief benchmark discussion is in [benchmarks](docs/benchmarks.md)

A change history, including differences from Clojure 1.8.0,
is in [changes](docs/changes.md).

## Dependency 

### Maven:

```xml
<dependency>
  <groupId>palisades-lakes</groupId>
  <artifactId>faster-multimethods</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Leiningen/Boot:
```clojure
[palisades-lakes/faster-multimethods "0.1.0"]
```

## Code examples

### Fastest:

```clojure
(require `[palisades.lakes.multimethods.core :as plm])

(plm/defmulti intersects?
  "Test for general set intersection."
  {}  
  (fn intersects?-dispatch [s0 s1] (plm/signature s0 s1))
  :hierarchy false)
  
(plm/defmethod intersects? 
  (plm/to-signature IntegerInterval java.util.Set)
  [^IntegerInterval s0 ^java.util.Set s1]
  (.intersects s0 s1))
...
```

### Most general:

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





