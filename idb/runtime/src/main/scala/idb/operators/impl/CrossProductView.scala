package idb.operators.impl

import idb.operators.CrossProduct
import idb.{MaterializedView, Table, Relation}
import idb.observer.{Observable, NotifyObservers, Observer}

/**
 * The cross product does not really require the underlying relations to be materialized directly.
 * But it requires some point of materialization.
 */
class CrossProductView[DomainA, DomainB, Range](val left: Relation[DomainA],
												val right: Relation[DomainB],
												val projection: (DomainA, DomainB) => Range,
												override val isSet: Boolean)
	extends CrossProduct[DomainA, DomainB, Range] with NotifyObservers[Range] {

	left addObserver LeftObserver
	right addObserver RightObserver

	private var leftList : List[DomainA] = Nil
	private var rightList : List[DomainB] = Nil

	override protected def lazyInitialize() {
		/* do nothing */
	}

	override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
		if (o == left) {
			return List(LeftObserver)
		}
		if (o == right) {
			return List(RightObserver)
		}
		Nil
	}

	/**
	 * Applies f to all elements of the view.
	 */
	override def foreach[T](f: (Range) => T) {
		left.foreach(
			a => {
				right.foreach(
					b => {
						f(projection(a, b))
					}
				)
			}
		)
	}

	object LeftObserver extends Observer[DomainA] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on left relation
		override def updated(oldA: DomainA, newA: DomainA) {
			var l1 : List[Range] = Nil
			var l2 : List[Range] = Nil

			right.foreach(
				b => {
					l1 = projection(oldA, b) :: l1
					l2 = projection(newA, b) :: l2
				}
			)

			l1 foreach notify_removed
			l2 foreach notify_added
		}

		override def removed(v: DomainA) {
			var l : List[Range] = Nil

			right.foreach(
				b => {
					l = projection(v, b) :: l
				}
			)

			l.foreach(notify_removed)

		//	if (left == right)
		//		notify_removed(projection(v,v.asInstanceOf[DomainB]))
		}

		override def added(v: DomainA) {
			var l : List[Range] = Nil //STore changes in list, because of possible recursive changes to right

			right.foreach(
				b => {
					l = projection(v, b) :: l
				}
			)

			l.foreach(notify_added)
		}
	}

	object RightObserver extends Observer[DomainB] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on right relation
		override def updated(oldB: DomainB, newB: DomainB) {
			var l1 : List[Range] = Nil
			var l2 : List[Range] = Nil

			left.foreach(
				a => {
					l1 = projection(a, oldB) :: l1
					l2 = projection(a, newB) :: l2
				}
			)

			l1 foreach notify_removed
			l2 foreach notify_added
		}

		override def removed(v: DomainB) {
			var l : List[Range] = Nil

			left.foreach(
				a => {
					l = projection(a, v) :: l
				}
			)

			l foreach notify_removed

		//	if (left == right)
		//		notify_removed(projection(v.asInstanceOf[DomainA],v))
		}

		override def added(v: DomainB) {
			var l : List[Range] = Nil //STore changes in list, because of possible recursive changes to left

			left.foreach(
				a => {
					l = projection(a, v) :: l
				}
			)

			l.foreach(notify_added)
		}
	}


}

object CrossProductView {
	def apply[DomainA, DomainB](left: Relation[DomainA], right: Relation[DomainB], isSet: Boolean) = {

		val l = if (left.isInstanceOf[MaterializedView[_]]) left else left.asMaterialized
		val r = if (right.isInstanceOf[MaterializedView[_]]) right else right.asMaterialized

		new CrossProductView[DomainA, DomainB, (DomainA, DomainB)](l,r,(l : DomainA, r : DomainB) => (l,r),isSet)
	}
}