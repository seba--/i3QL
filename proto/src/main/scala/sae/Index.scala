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
 *
 */
trait Index[K <: AnyRef, V <: AnyRef]
        extends MaterializedView[(K, V)]
           with SelfMaintainedView[V, (K, V)]
{

    val relation : MaterializedView[V]

    val keyFunction : V => K

    //  TODO inidices should be updated prior to other relations; re-using only the observers will yield update order considerations where clients are foreced to rely on the index as underlying collection instead of the collection itself
    relation addObserver this

    // an index is lazy initialized by calling build
    def lazyInitialize : Unit =
        {
            relation.foreach(v =>
                {
                    put_internal(keyFunction(v), v)
                }
            )
            initialized = true
        }

    protected def put_internal(key : K, value : V) : Unit

    def get(key : K) : Option[Traversable[V]] =
        {
            if (!initialized) {
                this.lazyInitialize
            }
            get_internal(key)
        }

    protected def get_internal(key : K) : Option[Traversable[V]]

    def isDefinedAt(key : K) : Boolean =
        {
            if (!initialized) {
                this.lazyInitialize
            }
            isDefinedAt_internal(key)
        }

    protected def isDefinedAt_internal(key : K) : Boolean


    def elementCountAt(key : K) : Int =
        {
            if (!initialized) {
                this.lazyInitialize
            }
            elementCountAt_internal(key)
        }

    protected def elementCountAt_internal(key : K) : Int


    def getOrElse(key : K, f : => Iterable[V]) : Traversable[V] = get(key).getOrElse(f)

    def updated_internal(oldV : V, newV : V) : Unit =
        {
            if (oldV == newV)
                return
            val k1 = keyFunction(oldV)
            val k2 = keyFunction(newV)
            if (k1 == k2) {
                update_element(k1, oldV, newV)
                element_updated((k1, oldV), (k1, newV))
            } else {
                remove_element((k1, oldV))
                element_removed((k1, oldV))
                add_element((k2, newV))
                element_added((k2, newV))
            }
        }

    def removed_internal(v : V) : Unit =
        {
            val k = keyFunction(v)
            remove_element((k, v))
            element_removed((k, v))
        }

    def added_internal(v : V) : Unit =
        {
            val k = keyFunction(v)
            add_element(k, v)
            element_added((k, v))
        }

    def add_element(kv : (K, V)) : Unit

    def remove_element(kv : (K, V)) : Unit

    def update_element(key : K, oldV : V, newV : V) : Unit

}