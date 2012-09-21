package sae.capabilities

import sae.Observable

trait Iterable[V]
    extends Observable[V]
{

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (V) => T)

}