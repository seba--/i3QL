package sae

import collection.mutable.HashSet

trait Observable[V <: AnyRef]
{

    protected[sae] var observers: HashSet[Observer[V]] = new HashSet[Observer[V]]()

    def addObserver(o: Observer[V]) {
        observers.add(o)
    }

    def removeObserver(o: Observer[V]) {
        observers.remove(o)
    }

    def clearObservers() {
        observers = HashSet.empty
    }

    def hasObservers = {
        !observers.isEmpty
    }

    /**
     * remove all observers
     */
    def clearObserversForChildren(visitChild: Observable[_ <: AnyRef] => Boolean) {
        for (observable <- children) {
            // remove all observers for this observable
            for (observer <- childObservers(observable)) {
                observable.removeObserver(observer.asInstanceOf[Observer[Any]])
            }
            // check whether we want to visit the observable
            if (observable.observers.isEmpty && visitChild(observable)) {
                observable.clearObserversForChildren(visitChild)
            }
        }
    }

    protected def children: Seq[Observable[_ <: AnyRef]] = Nil

    protected def childObservers(o: Observable[_]): Seq[Observer[_]] = Nil

    // Notify methods to notify the observers
    def element_added(v: V) {
        observers.foreach(_.added(v))
    }

    def element_removed(v: V) {
        observers.foreach(_.removed(v))
    }

    def element_updated(oldV: V, newV: V) {
        observers.foreach(_.updated(oldV, newV))
    }

}

