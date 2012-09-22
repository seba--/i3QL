package sae
package operators.impl

import operators.CrossProduct

/**
 * The cross product requires underlying relations to be materialized, but is not a materialized view in itself.
 */
class CrossProductView[DomainA <: AnyRef, DomainB <: AnyRef](val left: MaterializedRelation[DomainA],
                                                             val right: MaterializedRelation[DomainB])
    extends CrossProduct[DomainA, DomainB]
    with LazyInitializedRelation
{

    left addObserver LeftObserver
    right addObserver RightObserver

    /**
     * Each view must be able to
     * materialize it's content from the underlying
     * views.
     * The laziness allows a query to be set up
     * on relations (tables) that are already filled.
     * The lazy initialization must be performed prior to processing the
     * first add/delete/update events or foreach calls.
     */
    def lazyInitialize() {

    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: ((DomainA, DomainB)) => T) {
        left.foreach (
            a => {
                right.foreach (
                    b => {
                        element_added ((a, b))
                    }
                )
            }
        )
    }

    object LeftObserver extends LazyInitializedObserver[DomainA]
    {
        def lazyInitialize() {
            CrossProductView.this.lazyInitialize ()
        }

        /**
         * Returns true if initialization is complete
         */
        def isInitialized = CrossProductView.this.isInitialized

        /**
         * Set the initialized status to true
         */
        def setInitialized() {
            CrossProductView.this.setInitialized ()
        }

        // update operations on left relation
        override def updated(oldA: DomainA, newA: DomainA) {
            right.foreach (
                b => {
                    element_removed (oldA, b)
                    element_added (newA, b)
                }
            )
        }

        override def removed(v: DomainA) {
            right.foreach (
                b => {
                    element_removed (v, b)
                }
            )
        }

        override def added(v: DomainA) {
            right.foreach (
                b => {
                    element_added (v, b)
                }
            )
        }
    }

    object RightObserver extends LazyInitializedObserver[DomainB]
    {
        def lazyInitialize() {
            CrossProductView.this.lazyInitialize ()
        }

        /**
         * Returns true if initialization is complete
         */
        def isInitialized = CrossProductView.this.isInitialized

        /**
         * Set the initialized status to true
         */
        def setInitialized() {
            CrossProductView.this.setInitialized ()
        }

        // update operations on right relation
        override def updated(oldB: DomainB, newB: DomainB) {
            left.foreach (
                a => {
                    element_removed (a, oldB)
                    element_added (a, newB)
                }
            )

        }

        override def removed(v: DomainB) {
            left.foreach (
                a => {
                    element_removed (a, v)
                }
            )
        }

        override def added(v: DomainB) {
            left.foreach (
                a => {
                    element_added (a, v)
                }
            )
        }
    }


}