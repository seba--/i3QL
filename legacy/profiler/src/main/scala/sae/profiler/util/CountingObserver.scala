package sae.profiler.util

import sae.Observer

/**
 *
 * Author: Ralf Mitschke
 * Created: 16.06.11 11:02
 *
 * An Observer that merely counts the number of facts added to it
 */
class CountingObserver[V]
        extends Observer[V]
{
    private var counter = 0

    def count = counter

    def added(v: V)
    {
        counter += 1
    }

    def removed(v: V)
    {
        // do nothing
    }

    def updated(oldV: V, newV: V)
    {
        // do nothing
    }
}