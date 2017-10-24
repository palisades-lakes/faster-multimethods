# change history

## 0.1.1

### Clojure 1.9.0 compatibility

- Macros that expand into `\`(fn foo [] ...)` need to 
ensure `foo` is not namespace qualified, ie,
`\`(fn ~'foo [] ...)`

### JDK 9 compatibility

#### illegal reflective access

```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.parboiled.transform.AsmUtils (file:/C:/porta/projects/faster-multimethods/lib/parboiled-java-1.1.8.jar) to method java.lang.ClassLoader.findLoadedClass(java.lang.String)
WARNING: Please consider reporting this to the maintainers of org.parboiled.transform.AsmUtils
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```

can be eliminated, at least for the present, by adding

`--add-opens java.base/java.lang=ALL-UNNAMED`

to the call to `java` in the launcher scripts.

## changes from Clojure 1.8.0

### performance improvements

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
future developers, so is perhaps not the best choice.
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
It is possible that performance improvements to
Clojure collections would make this little library unnecessary.

2. Permit more efficient dispatch values (`signatures` in the plots).

    I've chosen to add support for an additional special case of
    dispatch values,
    which I call Signatures --- essentially short lists of Classes.
    
    This is backwards compatible; and can be adopted by changing
    the dispatch function from, eg, `[(class a) (class b)]` to
    `(signature a b)`.
    
3. Permit a `:hierarchy false` option to `defmulti`
(`nohierarchy` in the plots).

    Every multimethod (instance of MultiFn) contains a reference
    to a `Var` holding a `hierarchy`. This shared mutable state must
    be checked for changes every time a multimethod is called.
    
    An important special case is one where only classes,
    and sequences of classes
    are used as dispatch values. In this case, the hierarchy
    is irrelevant. 
    
    Removing the need for synchronizing with the `hierarchy`
    further reduces the overhead.
    
### semantic changes

The current implementation is not quite backwards compatible with 
Clojure 1.8.0, because it adds some input validation,
fixes 2 issues with `defmulti/defmethod`
and 4 issues with how the preferred method is found.

<ol>
<li> Most <code>MultiFn</code>
API methods here throw <code>IllegalArgumentException</code> if
the dispatch value(s) are not 'legal'. 

This is the most dubious of the changes I've made,
and most likely to cause problems to anyone who wants to 
migrate from <code>clojure.lang.MultiFn</code>.<br>

I would very much appreciate feedback on this issue.<br>
The Clojure version permits defining methods with anything
as the dispatch value, but only supports inheritance when
the dispatch values are <strong>namespace-qualified</strong>
instances of
<code>clojure.lang.Named</code>, classes, or 
an <code>IPersistentVector</code> containing legal dispatch 
values.

<li> Clojure 1.8.0 <code>defmethod</code> accepts keywords as 
dispatch values that aren't namespace qualified, 
ones where <code>clojure.core/derive</code> would throw an exception: 
<a href="https://github.com/clojure/clojure/blob/master/test/clojure/test_clojure/multimethods.clj#L161">basic-multimethod-test</a>.
This affects the default dispatch value <code>:default</code>,
which for now is handled as a special case.
I'm tempted to replace it with <code>:clojure.core\default</code>, 
or something like that, but that would mean an extra step for 
converting code from Clojure multimethods to faster-multimethods.

<li> <code>:default</code>, which is not namespace qualified, 
is used to define
a default method, which is called when there are no applicable methods.
But the default method will also be called if the dispatch
function returns <code>:default</code>.

I suspect having a special fallback default method is an unnecessary
complication, and likely to hide simple coding errors,
especially since <code>defmulti</code> permits specifying 
something other than <code>:default</code> as the 
no-applicable-methods key.
Also, if there is a special no-applicable-methods method, 
it ought not to be entangled with the hierarchy- and class-based
method lookup. 

Despite my reservations, I'm leaving the implementation
as is at present, except that only namespace-qualified
symbols or keywords may be supplied as alternatives to
<code>:default</code> in <code>defmulti</code>.
</ol>

4 issues with [MultiFn.dominates](https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L126):

The first issue is pretty clearly a bug, the remaining might be
attributed to different expectations for how the 
partial ordering should behave.

<ol>
<li> <a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">MultiFn.prefers</a>
ignores the multimethod's internal hierarchy.
This is discussed in 
<a href="https://dev.clojure.org/jira/browse/CLJ-2234">CLJ-2234</a>,
which has a patch submitted.
The problem is that <code>prefers</code> calls <code>parents</code>
without passing a hierarchy, so it is evaluated relative to the
global hierarchy, rather than the multimethod's.
faster-multimethods contains unit tests demonstrating the
problem in Clojure 1.8.0 and verifying the fix.
I plan to raise issues for the 3 remaining, more debatable problems
once this one is resolved.

<li> 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L161">findAndCacheBestMethod</a>
doesn't correctly find the minima of the 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L126">dominates</a>
partial ordering.
It does a simple reduction of the <code>methodTable</code>, 
maintaining a single <code>bestEntry</code>.
If it encounters an entry that is not strictly ordered, 
in either direction,
relative to the <code>bestEntry</code>, then it throws an exception.
However, it's possible for there to be a later entry which
would dominate both.
What the reduction should do is maintain a set of current minima.
If the current entry dominates any elements of the set, then those 
should be removed.
If the current entry is not dominated by any element of the set,
then it should be added.
I haven't created a unit test for this issue yet, because I'm not sure 
how to reliably cause a spurious exception to be thrown.
</ol>
The remaining 2 problems could be attributed to differing expectations
for the transitivity of the
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L126">dominates</a>
relation.
My expectation is that 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L126">dominates</a>
should be transitive,
that is: <code>dominates(x,y)</code> and 
<code>dominates(y,z)</code> should imply <code>dominates(x,z)</code>.

The 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L126">dominates</a>
relation is implemented as
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L122">isA</a>
extended with
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a>
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L122">isA</a>
which combines Java inheritance with a Clojure hierarchy, is transitive.
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a>
is not.

The documentation for 
<a href="https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/prefer-method">prefer-method</a>
only specifies that <code>(prefer-method f x y)</code> results in methods
defined for <code>f</code> for dispatch value <code>x</code> will be preferred
to methods for <code>y</code>.
One could read this as meaning there is no implied transitivity,
so that the only pairs in the <code>prefers</code> relation are those
explicitly defined via <code>prefer-method</code>.
However, the code doesn't do that either.

```java
private boolean prefers(Object x, Object y) {
  IPersistentSet xprefs = (IPersistentSet) getPreferTable().valAt(x);
  if(xprefs != null && xprefs.contains(y))
    return true;
  for(ISeq ps = RT.seq(parents.invoke(y)); ps != null; ps = ps.next())
    {
    if(prefers(x, ps.first()))
      return true;
    }
  for(ISeq ps = RT.seq(parents.invoke(x)); ps != null; ps = ps.next())
    {
    if(prefers(ps.first(), y))
      return true;
    }
  return false;
  }
```

<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a> first 
checks to see if there is an explicit edge on the
prefer-method graph for <code>(x,y)</code>. If so, return true.
Fine so far.

<ol start=3>
<li> The 2nd step in 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a>
essentially iterates over the
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L122">isA</a>
ancestors of <code>y</code>, returning true if <code>x</code> 
is preferred to any ancestor of <code>y</code>. 
This implies that <code>(prefer-method f x Object)</code> will cause the
method for <code>x</code> to be preferred to a method defined for any other
Java class or interface <code>y</code>, as long as we don't have <code>isA(x,y)</code>.
The fix for this is to just remove the first <code>for</code> loop from 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">MultiFn.prefers</a>.
faster-multimethods contains a unit test demonstrating
the behavior of Clojure 1.8.0 and verifying the fix.

The 3rd step in 
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a>
checks to see if any
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L122">isA</a>
ancestor of <code>x</code> is preferred to <code>y</code>. 
In this way,
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a>
inherits the transitivity of
<a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L122">isA</a>.

<li> <a href="https://github.com/clojure/clojure/blob/clojure-1.8.0/src/jvm/clojure/lang/MultiFn.java#L105">prefers</a>
is missing the search needed to be completely transitive.
It needs to check all the keys <code>k</code> of the <code>preferTable</code>,
returning true if <code>prefers(x,k)</code> and <code>prefers(k,y)</code>.
faster-multimethod contains a unit test demonstrating
the non-transitivity of Clojure 1.8.0, and verifying the transitivity
of its own implementation.
</ol>