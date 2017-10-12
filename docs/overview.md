# overview

Alternative to the 
Clojure 1.8.0 implementation of generic functions (aka multimethods)
via  `defmulti`/`defmethod`/`MultiFn`.

Very roughly 1/10 the cost for method lookup of Clojure 1.8.0,
and comparable in performance to using protocols, while being
fully dynamic.

- [benchmark summary](benchmarks.md)

- [change history](changes.md),
including differences from Clojure 1.8.0

- [method lookup](lookup.md),
details about dispatch values, partial ordering, and method lookup.

- [javadoc](javadoc/index.html)

## dependency 

### maven:

```xml
<dependency>
  <groupId>palisades-lakes</groupId>
  <artifactId>faster-multimethods</artifactId>
  <version>0.1.0</version>
</dependency>
```

### leiningen/boot:
```clojure
[palisades-lakes/faster-multimethods "0.1.0"]
```

## code examples

### fastest:

```clojure
(require `[palisades.lakes.multimethods.core :as plm])

(plm/defmulti intersects?
  "Test for general set intersection."
  {}  
  plm/signature
  :hierarchy false)
  
(plm/defmethod intersects? 
  (plm/to-signature IntegerInterval java.util.Set)
  [^IntegerInterval s0 ^java.util.Set s1]
  (.intersects s0 s1))
...
```

### most general:

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

## acknowledgments

### ![Yourkit](https://www.yourkit.com/images/yklogo.png)

YourKit is kindly supporting open source projects with its full-featured Java
Profiler.

YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

* <a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
* <a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.





