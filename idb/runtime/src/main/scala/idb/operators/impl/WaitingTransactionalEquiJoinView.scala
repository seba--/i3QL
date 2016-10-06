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

import util.TransactionKeyValueObserver
import idb.Relation
import idb.operators.EquiJoin
import idb.observer.{Observer, Observable, NotifyObservers}


class WaitingTransactionalEquiJoinView[DomainA, DomainB, Range, Key](val left: Relation[DomainA],
                                                              val right: Relation[DomainB],
                                                              val leftKey: DomainA => Key,
                                                              val rightKey: DomainB => Key,
                                                              val projection: (DomainA, DomainB) => Range,
															  override val isSet : Boolean)
    extends EquiJoin[DomainA, DomainB, Range, Key]
	with NotifyObservers[Range]
{

    left addObserver LeftObserver

    right addObserver RightObserver

    override def children() = List (left, right)

	override def lazyInitialize() {

	}

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver)
        }
        if (o == right) {
            return List (RightObserver)
        }
        Nil
    }

    override protected def resetInternal(): Unit = ???


    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Range) => T) {
        val idx = com.google.common.collect.ArrayListMultimap.create[Key, DomainA]()
        left.foreach (v =>
            idx.put (leftKey (v), v)
        )
        right.foreach (v => {
            val k = rightKey (v)
            if (idx.containsKey (k)) {
                val leftElements = idx.get (k)
                val it = leftElements.iterator ()
                while (it.hasNext) {
                    val l = it.next ()
                    f (projection (l, v))
                }
            }
        }
        )
    }

    private def doJoinAndCleanup() {
        joinAdditions ()
        joinDeletions ()
        LeftObserver.clear ()
        RightObserver.clear ()
    }

    private def joinAdditions() {
        val it: java.util.Iterator[java.util.Map.Entry[Key, DomainA]] = LeftObserver.additions.entries ().iterator
        var result:Seq[Range] = Nil
        while (it.hasNext) {
            val next = it.next ()
            val left = next.getValue
            val k = next.getKey
            if (RightObserver.additions.containsKey (k)) {
                val rightElements = RightObserver.additions.get (k)
                val it = rightElements.iterator ()
                while (it.hasNext) {
                    val right = it.next ()
                    result = (projection (left, right)) +: result
                }
            }
        }
        notify_addedAll(result)
    }

    private def joinDeletions() {
        val it: java.util.Iterator[java.util.Map.Entry[Key, DomainA]] = LeftObserver.deletions.entries ().iterator
        var result:Seq[Range] = Nil
        while (it.hasNext) {
            val next = it.next ()
            val left = next.getValue
            val k = next.getKey
            if (RightObserver.deletions.containsKey (k)) {
                val rightElements = RightObserver.deletions.get (k)
                val it = rightElements.iterator ()
                while (it.hasNext) {
                    val right = it.next ()
                    result = (projection (left, right)) +: result
                }
            }
        }
        notify_removedAll(result)
    }


    var leftFinished  = false
    var rightFinished = false

    object LeftObserver extends TransactionKeyValueObserver[Key, DomainA]
    {

        override def endTransaction() {
            // println (this + ".endTransaction() with " + observers)
            // println ("waiting : " + !rightFinished)

            leftFinished = true
            if (rightFinished) {
                doJoinAndCleanup ()

                notify_endTransaction ()


                leftFinished = false
                rightFinished = false
            }
        }

        def keyFunc = leftKey

        override def toString: String = WaitingTransactionalEquiJoinView.this.toString + "$LeftObserver"
    }

    object RightObserver extends TransactionKeyValueObserver[Key, DomainB]
    {

        override def endTransaction() {
            //  println(this + ".endTransaction() with " + observers)
            //  println("waiting : " + !leftFinished)

            rightFinished = true
            if (leftFinished) {
                doJoinAndCleanup ()
                notify_endTransaction ()

                leftFinished = false
                rightFinished = false
            }
        }

        def keyFunc = rightKey

        override def toString: String = WaitingTransactionalEquiJoinView.this.toString + "$RightObserver"
    }

}
