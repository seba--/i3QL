package sae
package operators.impl

import idb.operators.CrossProduct
import deltas.{Update, Deletion, Addition}

/**
 * The cross product does not really require the underlying relations to be materialized directly.
 * But it requires some point of materialization.
 */
class CrossProductView[DomainA, DomainB, Range](val left: Relation[DomainA],
                                                val right: Relation[DomainB],
                                                val projection: (DomainA, DomainB) => Range)
    extends CrossProduct[DomainA, DomainB, Range]
{

    left addObserver LeftObserver

    right addObserver RightObserver


    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver)
        }
        if (o == right) {
            return List (RightObserver)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Range) => T) {
        left.foreach (
            a => {
                right.foreach (
                    b => {
                        f (projection (a, b))
                    }
                )
            }
        )
    }

    object LeftObserver extends Observer[DomainA]
    {
        override def endTransaction() {
            notifyEndTransaction ()
        }

        // update operations on left relation
        def updated(oldA: DomainA, newA: DomainA) {
            right.foreach (
                b => {
                    element_removed (projection (oldA, b))
                    element_added (projection (newA, b))
                }
            )
        }

        def removed(v: DomainA) {
            right.foreach (
                b => {
                    element_removed (projection (v, b))
                }
            )
        }

        def added(v: DomainA) {
            right.foreach (
                b => {
                    element_added (projection (v, b))
                }
            )
        }

        def updated[U <: DomainA](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: DomainA](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

    object RightObserver extends Observer[DomainB]
    {
        override def endTransaction() {
            notifyEndTransaction ()
        }

        // update operations on right relation
        def updated(oldB: DomainB, newB: DomainB) {
            left.foreach (
                a => {
                    element_removed (projection (a, oldB))
                    element_added (projection (a, newB))
                }
            )

        }

        def removed(v: DomainB) {
            left.foreach (
                a => {
                    element_removed (projection (a, v))
                }
            )
        }

        def added(v: DomainB) {
            left.foreach (
                a => {
                    element_added (projection (a, v))
                }
            )
        }

        def updated[U <: DomainB](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: DomainB](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }


}