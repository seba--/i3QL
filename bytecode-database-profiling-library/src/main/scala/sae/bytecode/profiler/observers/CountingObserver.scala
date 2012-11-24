package sae.bytecode.profiler.observers

import sae.Observer
import sae.deltas.{Update, Deletion, Addition}

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
        counter -= 1
    }

    def updated(oldV: V, newV: V)
    {
        // do nothing
    }

    def updated[U <: V](update: Update[U]) {}

    def modified[U <: V](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        counter += additions.map (_.count).sum
        counter -= deletions.map (_.count).sum
    }
}