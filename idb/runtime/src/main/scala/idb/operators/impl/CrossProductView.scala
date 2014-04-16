package idb.operators.impl

import idb.operators.CrossProduct
import idb.{MaterializedView, Table, Relation}
import idb.observer.{Observable, NotifyObservers, Observer}
import com.google.common.collect.{ConcurrentHashMultiset, Multiset}

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

	private val leftSet : Multiset[DomainA] = ConcurrentHashMultiset.create()
	private val rightSet : Multiset[DomainB] = ConcurrentHashMultiset.create()

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
			if (!leftSet.remove(oldA))
				throw new IllegalStateException("Removed element not in relation: " + oldA)

			leftSet.add(newA)
			val it = rightSet.iterator()
			while(it.hasNext) {
				val b = it.next
				notify_updated(projection(oldA,b), projection(newA,b))
			}
		}

		override def removed(v: DomainA) {
			if (!leftSet.remove(v))
				throw new IllegalStateException("Removed element not in relation: " + v)

			val it = rightSet.iterator()
			while(it.hasNext) {
				val b = it.next
				notify_removed(projection(v,b))
			}
		}

		override def added(v: DomainA) {
			leftSet.add(v)
			val it = rightSet.iterator()
			while(it.hasNext) {
				val b = it.next
				notify_added(projection(v,b))
			}
		}
	}

	object RightObserver extends Observer[DomainB] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on right relation
		override def updated(oldB: DomainB, newB: DomainB) {
			if (!rightSet.remove(oldB))
				throw new IllegalStateException("Removed element not in relation: " + oldB)

			rightSet.add(newB)
			val it = leftSet.iterator()
			while(it.hasNext) {
				val a = it.next
				notify_updated(projection(a,oldB), projection(a,newB))
			}
		}

		override def removed(v: DomainB) {
			if (!rightSet.remove(v))
				throw new IllegalStateException("Removed element not in relation: " + v)

			val it = leftSet.iterator()
			while(it.hasNext) {
				val a = it.next
				notify_removed(projection(a,v))
			}
		}

		override def added(v: DomainB) {
			rightSet.add(v)
			val it = leftSet.iterator()
			while(it.hasNext) {
				val a = it.next
				notify_added(projection(a,v))
			}
		}
	}


}

object CrossProductView {
	def apply[DomainA, DomainB](left: Relation[DomainA], right: Relation[DomainB], isSet: Boolean) = {
		new CrossProductView[DomainA, DomainB, (DomainA, DomainB)](left,right,(l : DomainA, r : DomainB) => (l,r),isSet)
	}
}