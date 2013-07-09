/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.operators.impl

import scala.Some
import idb.{IndexService, Index, Relation}
import idb.operators.EquiJoin
import idb.observer.{NotifyObservers, Observer, Observable}


class EquiJoinView[DomainA, DomainB, Range, Key](val left: Relation[DomainA],
												 val right: Relation[DomainB],
												 val leftIndex: Index[Key, DomainA],
												 val rightIndex: Index[Key, DomainB],
												 val projection: (DomainA, DomainB) => Range,
												 override val isSet: Boolean)
	extends EquiJoin[DomainA, DomainB, Range, Key]
	with NotifyObservers[Range] {

	val leftKey: DomainA => Key = leftIndex.keyFunction
	val rightKey: DomainB => Key = rightIndex.keyFunction

	// we observe the indices, but the indices are not part of the observer chain
	// indices have a special semantics in order to ensure updates where all indices are updated prior to their observers

	leftIndex addObserver LeftObserver

	rightIndex addObserver RightObserver

	//override protected def children = List(leftIndex, rightIndex)

	override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
		if (o == leftIndex) {
			return List(LeftObserver)
		}
		if (o == rightIndex) {
			return List(RightObserver)
		}
		Nil
	}

	/**
	 * Applies f to all elements of the view.
	 */
	def foreach[T](f: (Range) => T) {
		if (leftIndex.size <= rightIndex.size) {
			leftEquiJoin(f)

		}
		else {
			rightEquiJoin(f)
		}
	}

	// use the left relation as keys, since this relation is smaller
	def leftEquiJoin[T](f: (Range) => T) {
		leftIndex.foreach(
		{
			case (key, v) =>
				rightIndex.get(key) match {
					case Some(col) => {
						col.foreach(u =>
							f(projection(v, u))
						)
					}
					case _ => // do nothing
				}
		}
		)
	}

	// use the right relation as keys, since this relation is smaller
	def rightEquiJoin[T](f: (Range) => T) {
		rightIndex.foreach(
		{
			case (key, u) =>
				leftIndex.get(key) match {
					case Some(col) => {
						col.foreach(v =>
							f(projection(v, u))
						)
					}
					case _ => // do nothing
				}
		}
		)
	}


	object LeftObserver extends Observer[(Key, DomainA)] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on left relation
		def updated(oldKV: (Key, DomainA), newKV: (Key, DomainA)) {
			val oldKey = oldKV._1
			val newKey = newKV._1
			val oldV = oldKV._2
			val newV = newKV._2
			if (oldV == newV)
				return // no change in value
			// change in value/ works also for change in key
			// could inline the second lookup to the Some(u) in first if no key changes are required
			rightIndex.get(oldKey) match {
				case Some(col) => {
					// the leftIndex was already updated so all entries previously mapped to oldKey are now mapped to newKey
					for (u <- col; i <- 1 to leftIndex.count(newKey)) {
						EquiJoinView.this.notify_removed(projection(oldV, u))
					}
				}
				case _ => // do nothing
			}
			rightIndex.get(newKey) match {
				case Some(col) => {
					// the leftIndex was already updated so all entries previously mapped to oldKey are now mapped to newKey
					for (u <- col; i <- 1 to leftIndex.count(newKey)) {
						EquiJoinView.this.notify_added(projection(newV, u))
					}
				}
				case _ => // do nothing
			}
		}

		def removed(kv: (Key, DomainA)) {
			rightIndex.get(kv._1) match {
				case Some(col) => {
					col.foreach(u =>

						notify_removed(projection(kv._2, u))
					)
				}
				case _ => // do nothing
			}
		}

		def added(kv: (Key, DomainA)) {
			rightIndex.get(kv._1) match {
				case Some(col) => {
					col.foreach(u =>

						notify_added(projection(kv._2, u))
					)
				}
				case _ => // do nothing
			}
		}

	}

	object RightObserver extends Observer[(Key, DomainB)] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on right relation
		def updated(oldKV: (Key, DomainB), newKV: (Key, DomainB)) {
			val oldKey = oldKV._1
			val newKey = newKV._1
			val oldV = oldKV._2
			val newV = newKV._2

			if (oldV == newV)
				return // no change in value
			// change in value/ works also for change in key

			// the update may require a larger amount of elements to be generated, due to bag semantics

			leftIndex.get(oldKey) match {
				case Some(col) => {
					// the rightIndex was already updated so all entries previously mapped to oldKey are now mapped to newKey
					for (u <- col; i <- 1 to rightIndex.count(newKey)) {
						notify_removed(projection(u, oldV))
					}
				}
				case _ => // do nothing
			}
			leftIndex.get(newKey) match {
				case Some(col) => {
					// the rightIndex was already updated so all entries previously mapped to oldKey are now mapped to newKey
					for (u <- col; i <- 1 to rightIndex.count(newKey)) {
						notify_added(projection(u, newV))
					}
				}
				case _ => // do nothing
			}
		}

		def removed(kv: (Key, DomainB)) {
			leftIndex.get(kv._1) match {
				case Some(col) => {
					col.foreach(u =>
						notify_removed(projection(u, kv._2))
					)
				}
				case _ => // do nothing
			}

		}

		def added(kv: (Key, DomainB)) {
			leftIndex.get(kv._1) match {
				case Some(col) => {
					col.foreach(u =>
						notify_added(projection(u, kv._2))
					)
				}
				case _ => // do nothing
			}

		}


	}

	protected def lazyInitialize() {}
}

object EquiJoinView {
	def apply[DomainA, DomainB](left: Relation[DomainA],
								right: Relation[DomainB],
								leftEq: Seq[(DomainA => Any)],
								rightEq: Seq[(DomainB => Any)],
								isSet: Boolean): Relation[(DomainA, DomainB)] = {

		val leftKey: DomainA => Seq[Any] = x => leftEq.map( f => f(x))
		val rightKey: DomainB => Seq[Any] = x => rightEq.map( f => f(x))

		val leftIndex: Index[Seq[Any], DomainA] = IndexService.getIndex(left, leftKey)
		val rightIndex: Index[Seq[Any], DomainB] = IndexService.getIndex(right, rightKey)

		return new EquiJoinView[DomainA, DomainB, (DomainA, DomainB), Seq[Any]](
			left,
			right,
			leftIndex,
			rightIndex,
			(_, _),
			isSet
		)
	}
}
