package idb.operators.impl

import idb.{MaterializedView, Relation}
import idb.operators.SymmetricDifference
import idb.observer.{NotifyObservers, Observer}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 08.07.13
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
class SymmetricDifferenceView[Domain](val left: MaterializedView[Domain],
									  val right: MaterializedView[Domain],
									  val isSet: Boolean)
	extends SymmetricDifference[Domain]
	with NotifyObservers[Domain] {

	left addObserver LeftObserver
	right addObserver RightObserver

	def foreach[T] (f: (Domain) => T) {
		left.foreach( l => {
			if(right.count(l) == 0)
				f(l)
		} )

		right.foreach( r => {
			if(left.count(r) == 0)
				f(r)
		})
	}

	protected def lazyInitialize () { }


	def children(): Seq[Relation[_]] = Seq (left,right)

	override protected def resetInternal(): Unit = ???



	object LeftObserver extends Observer[Domain] {
		def updated(oldV: Domain, newV: Domain) {
			notify_updated(oldV, newV)
		}

		def removed(v: Domain) {
			if(right.count(v) == 0) {
				notify_removed(v)
			} else {
				notify_added(v)
			}
		}

		def added(v: Domain) {
			if(right.count(v) == 0) {
				notify_added(v)
			} else {
				notify_removed(v)
			}
		}

    def addedAll(vs: Seq[Domain]) {
      val (added,removed) = vs partition(right.count(_) == 0)
      notify_removedAll(removed)
      notify_addedAll(added)
    }

    def removedAll(vs: Seq[Domain]) {
      val (removed,added) = vs partition(right.count(_) == 0)
      notify_removedAll(removed)
      notify_addedAll(added)
    }

		def endTransaction() {
			notify_endTransaction()
		}
	}

	object RightObserver extends Observer[Domain] {
		def updated(oldV: Domain, newV: Domain) {
			notify_updated(oldV, newV)
		}

		def removed(v: Domain) {
			if(left.count(v) == 0) {
				notify_removed(v)
			} else {
				notify_added(v)
			}
		}

		def added(v: Domain) {
			if(left.count(v) == 0) {
				notify_added(v)
			} else {
				notify_removed(v)
			}
		}

    def addedAll(vs: Seq[Domain]) {
      val (added,removed) = vs partition(left.count(_) == 0)
      notify_removedAll(removed)
      notify_addedAll(added)
    }

    def removedAll(vs: Seq[Domain]) {
      val (removed,added) = vs partition(left.count(_) == 0)
      notify_removedAll(removed)
      notify_addedAll(added)
    }

		def endTransaction() {
			notify_endTransaction()
		}
	}


}

