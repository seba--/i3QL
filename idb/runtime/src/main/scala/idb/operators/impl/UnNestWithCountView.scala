package idb.operators.impl

import idb.Relation
import idb.observer.{Observable, NotifyObservers, Observer}
import idb.operators.UnNestWithCount

/**
 * @author Mirko Köhler
 */
class UnNestWithCountView[Domain, Range] (
	val relation: Relation[Domain],
	val unNestFunction: Domain => Traversable[Range],
	override val isSet: Boolean
)
	extends UnNestWithCount[Domain, Range]
	with Observer[Domain]
	with NotifyObservers[(Domain, Int, Range)]
{

	relation.addObserver (this)

	override def lazyInitialize () {

	}

	override def endTransaction () {
		notify_endTransaction ()
	}

	override protected def childObservers (o: Observable[_]): Seq[Observer[_]] = {
		if (o == relation) {
			return List (this)
		}
		Nil
	}

	/**
	 * Applies f to all elements of the view.
	 */
	def foreach[T] (f: ((Domain, Range)) => T) {
		relation.foreach ((v: Domain) =>
			unNestFunction (v).foreach ((u: Range) =>
				f ((v, u))
			)
		)
	}


	def updated (oldV: Domain, newV: Domain) {
		removed (oldV)
		added (newV)
	}

	def removed (v: Domain) {
		var i = 0
		unNestFunction (v).foreach ((u: Range) => {
			notify_removed ((v, i, u))
			i = i + 1
		}
		)
	}

	def added (v: Domain) {
		var i = 0
		unNestFunction (v).foreach ((u: Range) => {
			notify_added((v, i, u))
			i = i + 1
		})
	}
}

object UnNestView
{
	def apply[Domain, Range] (
								 relation: Relation[Domain],
								 unNestFunction: Domain => Traversable[Range],
								 isSet: Boolean
								 ): UnNestView[Domain, Range] = {
		new UnNestView[Domain, Range](relation, unNestFunction, isSet)
	}

}
