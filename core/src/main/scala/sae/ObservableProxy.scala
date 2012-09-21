package sae

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.05.11 12:35
 *
 * The ObservableProxy forwards all observer updates to the internal relation
 */
trait ObservableProxy[V <: AnyRef] extends Observable[V]
{

    protected def relation: Observable[V]

    relation.addObserver(ProxyObserver)

    override protected def children = List(relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(ProxyObserver)
        }
        Nil
    }

    private object ProxyObserver extends Observer[V]
    {
        def added(v: V)
        {
            element_added(v)
        }

        def removed(v: V)
        {
            element_removed(v)
        }

        def updated(oldV: V, newV: V)
        {
            element_updated(oldV, newV)
        }
    }
}

// proxy observer for indices
class HashIndexedViewProxyOLD[V <: AnyRef](val relation: OLDMaterializedView[V])
        extends IndexedViewOLD[V]
        with ObservableProxy[V]
{

    initialized = true

    def lazyInitialize {
        // do nothing
    }

    def materialized_foreach[T](f: (V) => T) {
        relation.foreach(f)
    }

    def materialized_size: Int = relation.size

    def materialized_singletonValue: Option[V] = relation.singletonValue

    protected def createIndex[K <: AnyRef](keyFunction: V => K): Index[K, V] =
    {
        val index = new sae.collections.HashBagIndex[K, V](relation, keyFunction)
        index.lazyInitialize
        index
    }
        

    protected def materialized_contains(v: V) = relation.contains(v)
}