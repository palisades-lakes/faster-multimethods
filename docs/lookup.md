# method lookup

A _multimethod_ is a Clojure function 
(an instance of `palisades.lakes.multimethods.java.MultiFn`
which implements `clojure.lang.IFn`) which, when invoked:

1. First applies a _dispatch function_ to the arguments, 
returning a _dispatch value_. 

2. Finds the  _appplicable methods_ 
from a table mapping dispatch values 
to functions.

3. Finds the minima of a preference partial ordering of
the applicable methods.

4. If there is a unique minimal method, 
it applies that to the arguments.
If there are no applicable methods, or more than one minimal 
method, an exception is thrown.
 
The purpose of this document is to explain the process in more
detail.

Note: I believe what I describe here is more complicated than necessary,
due to a desire to remain (mostly) consistent with Clojure 1.8.0
behavior. I've changed certain aspects of the behavior that
I believe are unintended bugs --- see 
[changes](https://github.com/palisades-lakes/faster-multimethods/blob/master/docs/changes.md)
for a detailed discussion. of the differences.

## dispatch function

The dispatch function is responsible for returning 
_legal dispatch values_.

**Warning:** 
Because we want method lookup to be fast,
the current implementation (like Clojure 1.8.0)
doesn't validate dispatch values before method lookup. 
Instead, a possibly mysterious looking exception will be thrown
some time later.
You can use `palisades.lakes.multimethods\legal-dispatch-value?`
in your dispatch function, permanently if you can afford the cost,
or at least in unit tests, and during development or debugging.
    
### legal dispatch values

Legal dispatch values are one of (my terminology):

- _atomic_ 

    - an instance of `Class`.
    - a namespace-qualified instance of `Named` (a `Symbol` or `Keyword`).

- `:default`: a special case of a non-namespace-qualified
keyword, for consistency with Clojure 1.8.0. Used to define
a default method which is called when there are no other applicable
methods. Dispatch functions may return `:default` to short cut
method lookup.

- _recursive_  

    an instance of `clojure.lang.IPersistentVector` 
    whose elements are legal dispatch values, 
    either atomic or recursive.

- _signature_

    an instance of `palisades.lakes.multimethods.java.Signature`,
    effectively an immutable `list` of classes.
    
    Atomic and recursive dispatch values are supported by Clojure
    1.8.0 multimethods.  
    
    Signatures, which are not recursive, are 
    added here to optimize the common special case of pure
    class-based method lookup for multiple arguments.
    The current version of signatures is not recursive,
    and only supports single arity method functions with simple
    arglists (no destructuring).
    
    (It may work to use a `Signature` as a leaf in a recursive
    dispatch value, but I haven't tested it, don't recommend it,
    and don't plan to support it. However, I open to arguments
    for why it would be worth the trouble.)
    
## dispatch value ordering
    
### applicable methods (isa<=)

Applicable methods are determined using 
a partial ordering of dispatch values I'm calling `isa<=`.
`isa<=` depends in general on a `Var` pointing
to a shared, mutable Clojure 
[hierarchy](https://clojure.org/reference/multimethods).

The premise is that any method of `f` 
defined for dispatch value `y`
could be applied to arguments resulting in dispatch value `x`,
as long as `(isa<= f x y)`.

([faster-multimethods](https://palisades-lakes.github.io/faster-multimethods/palisades.lakes.multimethods.core.html)
has a function of the same name, but that's provided for 
unit tests/debugging and isn't called during method lookup.)

- `(isa<= f s0 s1)` if `(.isAssignableFrom s1 s0)`,
when `s0` and `s1` are classes or signatures.

- `(isa<= f r0 r1)`, when `r0` and `r1` are recursive dispatch
values, if `r0` and `r1` are the same shape, and 
`(isa<= f a0 a1)` is true for every pair of corresponding atomic 
leaves of `r0` and `r1`.
 
- `(isa<= f a0 a1)` if `(clojure.core\isa? (.hierarchy f) a0 a1)`
when `a0` and `a1` are atomic.

    - When `a0` and `a1` are both classes, `isa?` ignores the
    hierarchy, and is just `(.isAssignableFrom a1 a0)`.
    
    - When `a0` is `Named` and `a1` is a `Class`, `isa?` returns 
    false. 
    
    - Otherwise, the hierarchy is used. A hierarchy is a directed
    acyclic graph created with calls to `clojure.core/derive`.
    Edges in this graph connect 2 `Named` or a `Class` and a `Named`.
    The `isa?` relation is the transitive closure of the 
    child-parent edge relation, that is, the descendant-ancestor
    relation. 
    
    **Note:** that `(isa? hierarchy c n)` may be true for 
    a `Class` `c` and
    a `Named` `n`, but not the other way around.

### preferred methods (dominates<)

The `dominates<` (pseudo-code) relation extends `isa<=`
with additional explicit child-parent pairs, created by calling 
`prefer-method`.

The explicit `prefer-method` child-parent pairs may 
have any two dispatch values for child and parent,
`Class`, `Named`, or recursive.

`(prefer-method d0 d1)` checks that `d1` is not already preferred
to `d0`, but otherwise allows any pair of values,
which might or might not be legal dispatch values,
 might not be the same shapes, etc.

`(prefer-method d0 d1)` is only called if there is some `d2`
such that `(isa<= d2 d0)` and `(isa<= d2 d1)`,
so edges relating illegal dispatch values, or dispatch values
of differing shapes, will have no effect.

On the other hand, the lack of validation in `prefer-method` is
likely to cause difficult-to-debug surprises later.

**Note:** The ordering of recursive dispatch values can only be 
changed for individual multimethods, making it harder to ensure 
consistent behavior in related multimethods, but at least limiting
the damage radius of ill-considered changes.

### default dispatch value

Finally, every multimethod has a special _default dispatch value_
(defaulting to `:default`).
A method defined for the _default dispatch value_ will be called
if there are no applicable methods.

## method lookup

Each multimethod contains a table mapping
certain dispatch values to the _defined methods_, 
themselves Clojure functions. 

The second step in calling a multimethod has 3 parts:

1. determine which of the defined methods are
`isa<=` applicable to the arguments' dispatch value.

    If there are no applicable methods, throw an exception.

2. find the `dominates<` minima (most preferred) 
among the applicable methods.

    If there is more than one minimal method, throw an exception.

3. apply the chosen method to the arguments.


   