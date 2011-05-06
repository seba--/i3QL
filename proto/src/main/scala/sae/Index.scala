package sae

/**
 * An index in a database provides fast access to the values <code>V</code>
 * of a relation of Vs via keys of type <code>K</code>.</br>
 * The index assumes that the underlying relation can change.
 * Thus each index is required to register as an observer of the base relation.
 * Updates to the relation must be propagated to observers of the index.
 * </br>
 * Due to the contravariant nature of observers
 * (i.e., an Observer[Object] can still register as an observer for this index),
 * the values have to remain invariant. But the relation may still vary.
 */
trait Index[K <: AnyRef, V <: AnyRef]
        extends Observer[V] {
    val relation : MaterializedView[V]

    val keyFunction : V => K

    relation addObserver this

    private var initialized = false

    // an index is lazy initialized by calling build
    private def build : Unit =
        {
            relation.foreach(v =>
                {
                    val prev = put_internal(keyFunction(v), v)
                    assert(prev == None)
                }
            )
            initialized = true
        }

    protected def put_internal(key : K, value : V) : Option[V]

    def get(key : K) : Option[V] =
        {
            if (!initialized) {
                this.build
            }
            get_internal(key)
        }

    protected def get_internal(key : K) : Option[V]

    def isDefinedAt(key : K) : Boolean =
        {
            if (!initialized) {
                this.build
            }
            isDefinedAt_internal(key)
        }

    protected def isDefinedAt_internal(key : K) : Boolean

    def getOrElse(key : K, f : => V) : V = get(key).getOrElse(f)

}