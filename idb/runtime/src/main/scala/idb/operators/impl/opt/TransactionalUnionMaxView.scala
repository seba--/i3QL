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
package idb.operators.impl.opt

import idb.operators.Union
import idb.Relation
import idb.observer.NotifyObservers
import idb.operators.impl.util.TransactionElementObserver


/**
 * A self maintained union, that produces count(A) + count(B) duplicates for underlying relations A and B
 */
class TransactionalUnionMaxView[Range, DomainA <: Range, DomainB <: Range](
																			  val left: Relation[DomainA],
																			  val right: Relation[DomainB],
																			  override val isSet: Boolean)
	extends Union[Range, DomainA, DomainB]
	with NotifyObservers[Range] {

	left addObserver LeftObserver
	right addObserver RightObserver

	var leftTransactionEnded: Boolean = false
	var rightTransactionEnded: Boolean = false

	override def lazyInitialize() {

	}

	override protected def resetInternal(): Unit = {
		clear()
	}

	private def clear() {
		LeftObserver.clear()
		RightObserver.clear()

		leftTransactionEnded = false
		rightTransactionEnded = false
	}

	private def doEndTransaction() {

		if (!leftTransactionEnded || !rightTransactionEnded)
			return

		//Update the additions of the transaction
		val itAddLeft = LeftObserver.additions.iterator()
		while (itAddLeft.hasNext) {
			val v = itAddLeft.next()
			if (LeftObserver.additions.count(v) > RightObserver.additions.count(v))
				notify_added(v)
		}

		val itAddRight = RightObserver.additions.iterator()
		while (itAddRight.hasNext) {
			val v = itAddRight.next()
			if (LeftObserver.additions.count(v) <= RightObserver.additions.count(v))
				notify_added(v)
		}

		//Update the deletions of the transaction
		val itDelLeft = LeftObserver.deletions.iterator()
		while (itDelLeft.hasNext) {
			val v = itDelLeft.next()
			if (LeftObserver.deletions.count(v) <= RightObserver.deletions.count(v))
				notify_removed(v)
		}

		val itDelRight = RightObserver.deletions.iterator()
		while (itDelRight.hasNext) {
			val v = itDelRight.next()
			if (LeftObserver.deletions.count(v) > RightObserver.deletions.count(v))
				notify_removed(v)
		}

		clear()
		notify_endTransaction()

	}

	object LeftObserver extends TransactionElementObserver[DomainA] {

		def endTransaction() {
			leftTransactionEnded = true
			doEndTransaction()
		}
	}

	object RightObserver extends TransactionElementObserver[DomainB] {

		def endTransaction() {
			rightTransactionEnded = true
			doEndTransaction()

		}
	}


	/**
	 * Applies f to all elements of the view.
	 */
	def foreach[T](f: (Range) => T) {
		//TODO implement foreach
	}
}
