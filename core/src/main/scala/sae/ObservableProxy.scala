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