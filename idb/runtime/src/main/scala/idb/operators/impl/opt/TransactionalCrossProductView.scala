package idb.operators.impl.opt

import idb.operators.CrossProduct
import idb.{Table, Relation}
import idb.observer.{Observable, NotifyObservers, Observer}
import idb.operators.impl.util.TransactionElementObserver

class TransactionalCrossProductView[DomainA, DomainB, Range](val left: Relation[DomainA],
												val right: Relation[DomainB],
												val projection: (DomainA, DomainB) => Range,
												override val isSet: Boolean)
	extends CrossProduct[DomainA, DomainB, Range] with NotifyObservers[Range] {

	left addObserver LeftObserver
	right addObserver RightObserver

	var leftTransactionEnded = false
	var rightTransactionEnded = false

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

	override protected def resetInternal(): Unit = {
		clear()
	}

	private def clear() {
		leftTransactionEnded = false
		rightTransactionEnded = false

		LeftObserver.clear()
		RightObserver.clear()
	}

	private def doEndTransaction() {
		if (!leftTransactionEnded || !rightTransactionEnded)
			return

		/*Update deletions*/
    var removed = Seq[Range]()
		val itDelLeft = LeftObserver.deletions.iterator()
		while (itDelLeft.hasNext) {
			val e1 = itDelLeft.next()
			val itDelRight = RightObserver.deletions.iterator()
			while (itDelRight.hasNext) {
				val e2 = itDelRight.next()
				removed = projection(e1,e2) +: removed
			}
		}

		/*Update additions*/
    var added = Seq[Range]()
		val itAddLeft = LeftObserver.additions.iterator()
		while (itAddLeft.hasNext) {
			val e1 = itAddLeft.next()
			val itAddRight = RightObserver.additions.iterator()
			while (itAddRight.hasNext) {
				val e2 = itAddRight.next()
				added = projection(e1,e2) +: added
			}
		}

    notify_removedAll(removed)
    notify_addedAll(added)

		clear()
		notify_endTransaction()
	}

	/**
	 * Applies f to all elements of the view.
	 */
	override def foreach[T](f: (Range) => T) {
		throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
	}

	object LeftObserver extends TransactionElementObserver[DomainA] {

		override def endTransaction() {
			leftTransactionEnded = true
			doEndTransaction()
		}
	}

	object RightObserver extends TransactionElementObserver[DomainB] {

		override def endTransaction() {
			rightTransactionEnded = true
			doEndTransaction()
		}
	}
}

object TransactionalCrossProductView {
	def apply[DomainA,DomainB](relationA : Relation[DomainA], relationB : Relation[DomainB], isSet : Boolean) : Relation[(DomainA,DomainB)] = {
		return new TransactionalCrossProductView[DomainA,DomainB,(DomainA,DomainB)](relationA, relationB, (a,b) => (a,b), isSet)
	}
}