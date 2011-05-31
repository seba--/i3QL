package sae

/**
 * 
 * Author: Ralf Mitschke
 * Created: 31.05.11 12:35
 *
 */
trait ObservableProxy[V <: AnyRef] extends Observable[V] {

    protected val relation : Observable[V]

    override def addObserver(o : Observer[V]) {
        relation addObserver o
    }

    override def removeObserver(o : Observer[V]) {
        relation removeObserver o
    }

    // Notify methods to notify the observers
    override def element_added(v : V) { throw new UnsupportedOperationException }
    override def element_removed(v : V) { throw new UnsupportedOperationException }
    override def element_updated(oldV : V, newV : V) { throw new UnsupportedOperationException }
}