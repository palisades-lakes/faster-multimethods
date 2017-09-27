# method lookup, dispatch values and functions

A _multimethod_ is a Clojure function 
(an instance of `clojure.lang.IFn`) which, when invoked:

1. First applies a _dispatch function_ to the arguments, 
returning a _dispatch value_. 

2. Then finds the  _appplicable method_ from a table mapping dispatch values 
to functions, using a partial ordering of the possible
dispatch values to determine which
methods are _applicable,_ and, among those, the minima
of the partial ordering.

3. If there is a unique minimal method, 
it applies that to the arguments.
If there are no applicable methods, or more than one minimal 
method, an exception is thrown.
 
### legal dispatch values

Legal dispatch values are one of (my terminology):

- _atomic_ 

    - an instance of `Class`.
    - a namespace-qualified instance of `Named` (a `Symbol` or `Keyword`).

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
    
### dispatch value ordering

Starts from ordering o atomic values and extended to recursive and
signatures.

Method lookup is done using 2 distinct partial orderings of
dispatch values (in pseudo-code `preferred?` is not an actual 
function):

- `(isa? hierarchy d0 d1)` determines whether a method defined for 
`d1` is applicable to the arguments producing the disptach value
`d0`.

- `(preferred? hierarchy d0 d1)`, extends the `isa?` ordering
with additional pairs introduced by calls to `prefer-method`.

#### isa?

The `isa?` relation is derived from a hierarchy in several steps:

1. Every hierarchy maintains an explicit directed acyclic graph 
(DAG) made up of child-parent edges
(created with calls to `derive`)
where the parent is an instance of `Named` and the child is 
an instance of `Named` or `Class`.
The `isa?` relations includes these child-parent pairs,
a relation on 
{all atomic dispatch values} X {`Named` dispatch values}.

2. The `isa?` relation is extended with implicit pairs 
corresponding to the `parent.isAssignableFrom(child)` 
relation between Java classes/interfaces. 
These pairs are a relation on
{`Class` dispatch values} X {`Class` dispatch values}.

3. The `atomic-isa?` relation (again pseudo-code)
is the transitive closure of the union of the relations in 
steps 1 and 2.

4. Finally, `atomic-isa?` is extended to `isa?`
on all legal dispatch values via recursion.
A pair of recursive dispatch values has an child-parent edge 
if they have the same shape
(the same number of elements at every level of nesting), 
and there is a child-parent edge in the original DAG for every
ordered pair of corresponding leaf atomic elements.

**Note:** `isa?` is derived from a hierarchy,
which may be shared by many multimethods,
especially in the default case, which uses the unique
global hierarchy,
and may be modified (via `derive`/`underive`) at any time,
in any thread.
This makes it easier to get consistent behavior from a group of
related multimethods, but harder to prevent unexpected 
side-effects from changes.

#### preferred?

The `preferred?` (pseudo-code) relation extends `isa?`
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
such that `(isa? d2 d0)` and `(isa? d2 d1)`,
so edges relating illegal dispatch values, or dispatch values
of differing shapes, will have no effect.

On the other hand, the lack of validation in `prefer-method` is
likely to cause difficult-to-debug surprises later.

**Note:** The ordering of recursive dispatch values can only be 
changed for individual multimethods, making it harder to ensure 
consistent behavior in related multimethods, but at least limiting
the damage radius of ill-considered changes.

#### default dispatch value

Finally, every multimethod has a special _default dispatch value_
(defaulting to `:default`).
A method defined for the _default dispatch value_ will be called
if there are no applicable methods.

This is likely a design mistake.

For example, `:default` might be a node 
in the hierarchy, with explicitly defined parents and children
in the `isa?` relation.
But the `:default` method will be called for dispatch values
where `(isa? d :default)` is `false`.
Possibly `(defdefault ...)` to define a method associated with no
dispatch value would be better.

## method lookup

Each multimethod contains a table mapping
certain dispatch values to the _defined methods_, 
themselves Clojure functions. 

The second step in calling a multimethod has 3 parts:

1. determine which of the defined methods are
`isa?` applicable to the arguments' dispatch value.

    If there are no applicable methods, throw an exception.

2. find the `preferred?` minima (most preferred) 
among the applicable methods.

    If there is more than one minimal method, throw an exception.

3. apply the chosen method to the arguments.

**Note:** Methods can be added to and removed from the method table
at any time (via `defmethod`/`remove-method` below).

   