package idb.operators.impl

import idb.operators.CrossProduct
import idb.{MaterializedView, Table, Relation}
import idb.observer.{Observable, NotifyObservers, Observer}
import com.google.common.collect.{ConcurrentHashMultiset, Multiset}

/**
 * The cross product does not really require the underlying relations to be materialized directly.
 * But it requires some point of materialization.
 */
case class CrossProductView[DomainA, DomainB, Range](val left: Relation[DomainA],
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

	override protected def resetInternal(): Unit = {
		leftSet.clear()
		rightSet.clear()
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

	//TODO: Reactivate exceptions in remove and update
	object LeftObserver extends Observer[DomainA] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on left relation
		override def updated(oldA: DomainA, newA: DomainA) {

			if (leftSet.remove(oldA)) {
				leftSet.add(newA)
				val it = rightSet.iterator()
				while(it.hasNext) {
					val b = it.next
					notify_updated(projection(oldA,b), projection(newA,b))
				}
			} else {
				throw new IllegalStateException("Removed element not in relation: " + oldA)
			}
		}

		override def removed(v: DomainA) {
			if (leftSet.remove(v)) {
				val it = rightSet.iterator()
				while(it.hasNext) {
					val b = it.next
					notify_removed(projection(v,b))
				}
			} else {
				throw new IllegalStateException("Removed element not in relation: " + v)
			}
		}

    override def removedAll(vs: Seq[DomainA]) {
      vs foreach (v => if(!leftSet.remove(v)) throw new IllegalStateException("Removed element not in relation: " + v))

      var removed = Seq[Range]()
      val it = rightSet.iterator()
      while(it.hasNext) {
        val b = it.next
        removed = removed ++ vs.map(projection(_, b))
      }
      notify_removedAll(removed)
    }

    override def added(v: DomainA) {
			leftSet.add(v)
			val it = rightSet.iterator()
			while(it.hasNext) {
				val b = it.next
				notify_added(projection(v,b))
			}
		}

    override def addedAll(vs: Seq[DomainA]) {
      for (v <- vs)
        leftSet.add(v)

      var added = Seq[Range]()
      val it = rightSet.iterator()
      while(it.hasNext) {
        val b = it.next
        added = added ++ vs.map(projection(_,b))
      }
      notify_addedAll(added)
    }
	}

	object RightObserver extends Observer[DomainB] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on right relation
		override def updated(oldB: DomainB, newB: DomainB) {
			if (rightSet.remove(oldB)) {
				rightSet.add(newB)
				val it = leftSet.iterator()
				while(it.hasNext) {
					val a = it.next
					notify_updated(projection(a,oldB), projection(a,newB))
				}
			} else {
				throw new IllegalStateException("Removed element not in relation: " + oldB)
			}
		}

		override def removed(v: DomainB) {
			if (rightSet.remove(v)) {
				val it = leftSet.iterator()
				while(it.hasNext) {
					val a = it.next
					notify_removed(projection(a,v))
				}
			} else {
				println(rightSet)
				throw new IllegalStateException("Removed element not in relation: " + v)
			}
		}

    override def removedAll(vs: Seq[DomainB]) {
      vs foreach (v => if(!rightSet.remove(v)) throw new IllegalStateException("Removed element not in relation: " + v))

      var removed = Seq[Range]()
      val it = leftSet.iterator()
      while(it.hasNext) {
        val a = it.next
        removed = removed ++ vs.map(projection(a,_))
      }
      notify_removedAll(removed)
    }

    override def added(v: DomainB) {
			rightSet.add(v)
			val it = leftSet.iterator()
			while(it.hasNext) {
				val a = it.next
				notify_added(projection(a,v))
			}
		}

    override def addedAll(vs: Seq[DomainB]) {
      for (v <- vs)
        rightSet.add(v)

      var added = Seq[Range]()
      val it = leftSet.iterator()
      while(it.hasNext) {
        val a = it.next
        added = added ++ vs.map(projection(a,_))
      }
      notify_addedAll(added)
    }
	}


}

object CrossProductView {
	def apply[DomainA, DomainB](left: Relation[DomainA], right: Relation[DomainB], isSet: Boolean) = {
		new CrossProductView[DomainA, DomainB, (DomainA, DomainB)](left,right,(l : DomainA, r : DomainB) => (l,r),isSet)
	}
}