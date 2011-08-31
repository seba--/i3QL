package sae

trait Observable[V <: AnyRef] 
{

    protected var observers = List[Observer[V]]();

    def addObserver(o : Observer[V]) {
        observers = o +: observers
    }

    def removeObserver(o : Observer[V]) {
        // not efficient, but probably no problem, because observers are typically not removed anyway, or?
        observers = observers.filterNot(_ eq o)
    }

    // Notify methods to notify the observers
    def element_added(v : V) { observers.foreach(_.added(v)) }
    def element_removed(v : V) { observers.foreach(_.removed(v)) }
    def element_updated(oldV : V, newV : V) { observers.foreach(_.updated(oldV, newV)) }

}

