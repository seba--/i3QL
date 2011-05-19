package sae

trait View[V <: AnyRef]
        extends Observable[V] {

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f : (V) => T)
    

}