package sae

/**
 *
 * Author: Ralf Mitschke
 * Date: 16.01.12
 * Time: 21:05
 * A static accessor for testing caching and observer removal
 */
object ObserverAccessor {

    def observerCount[V <: AnyRef](o:Observable[V]) = o.observers.size

}