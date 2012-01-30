package sae
package operators

import sae.collections.Bag

/**
 * A cross product constructs all combinations of tuples in multiple relations.
 * Thus the cross product dramatically enlarges
 * the amount of tuples in it's output.
 * The new relations are anonymous tuples of the
 * warranted size and types of the cross product.
 *
 * IMPORTANT: The cross product is not a self-maintained view.
 *            In order to compute the delta of adding a tuple
 *            to one of the underlying relations,
 *            the whole other relation needs to be considered.
 */
class CrossProduct[A <: AnyRef, B <: AnyRef](
                                                    val left: MaterializedView[A],
                                                    val right: MaterializedView[B])
        extends Bag[(A, B)]
{

    type LeftDomain = A

    type RightDomain = B

    left addObserver LeftObserver
    right addObserver RightObserver

    override protected def children = List(left, right).asInstanceOf[List[Observable[AnyRef]]]

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List(LeftObserver)
        }
        if (o == right) {
            return List(RightObserver)
        }
        Nil
    }

    def lazyInitialize: Unit = {
        if (initialized) return
        left.foreach(a => {
            right.foreach(b => {
                this.add_element(a, b)
            }
            )
        }
        )
        initialized = true
    }

    object LeftObserver extends Observer[A]
    {
        // update operations on left relation
        def updated(oldA: A, newA: A): Unit = {
            right.foreach(b => {
                CrossProduct.this -=(oldA, b)
                CrossProduct.this +=(newA, b)
            }
            )
        }

        def removed(v: A): Unit = {
            right.foreach(b => {
                CrossProduct.this -=(v, b)
            }
            )
        }

        def added(v: A): Unit = {
            right.foreach(b => {
                CrossProduct.this +=(v, b)
            }
            )
        }
    }

    object RightObserver extends Observer[B]
    {
        // update operations on right relation
        def updated(oldB: B, newB: B): Unit = {
            left.foreach(a => {
                CrossProduct.this -=(a, oldB)
                CrossProduct.this +=(a, newB)
            }
            )
        }

        def removed(v: B): Unit = {
            left.foreach(a => {
                CrossProduct.this -=(a, v)
            }
            )
        }

        def added(v: B): Unit = {
            left.foreach(a => {
                CrossProduct.this +=(a, v)
            }
            )
        }
    }

}