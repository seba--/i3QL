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

import collection.mutable
import idb.Relation
import idb.operators.FixPointRecursion
import idb.observer.NotifyObservers
import idb.operators.impl.util.TransactionKeyValueObserver

/**
 *
 *
 * @author Ralf Mitschke
 */
class TransactionalFixPointRecursionView[Domain, Range, Key](val source: Relation[Domain],
                                                             val anchorFunction: Domain => Option[Range],
                                                             val domainKeyFunction: Domain => Key,
                                                             val rangeKeyFunction: Range => Key,
                                                             val step: (Domain, Range) => Range,
															 override val isSet : Boolean)
  extends FixPointRecursion[Domain, Range, Key]
  with TransactionKeyValueObserver[Key, Domain]
	with NotifyObservers[Range]
{

  source addObserver this

  var additionAnchors: List[Range] = Nil
  var additionResults = mutable.HashSet.empty[Range]
  var deletionAnchors: List[Range] = Nil
  var deletionResults = mutable.HashSet.empty[Range]

  override def lazyInitialize() {

  }

  override protected def resetInternal(): Unit = {
    clear()
  }

  def keyFunc = domainKeyFunction


  def doRecursionForAddedElements() {

    for (anchor <- additionAnchors) {


      // you could try to optimize this by checking whether "additions.get(key).size() == 1" and in this case just doing a while loop


      addResult(anchor)

    }
  }

  private def addResult(newResult: Range) {


    //If the newResult is already present in additionResults, do nothing (fixed point).
    if (additionResults.contains(newResult)) {
      return
      //else combine the already present result with the new result.
    } else {
      additionResults.add(newResult)
      notify_added(newResult)

      var it: java.util.Iterator[Domain] = additions.get(rangeKeyFunction(newResult)).iterator()
      while (it.hasNext) {
        val next: Domain = it.next()
        val nextResult: Range = step(next, newResult)
        addResult(nextResult)
      }
    }


  }

  def doRecursionForRemovedElements() {
    // all domain values are stored in the Multimap "deletions"

    for (anchor <- deletionAnchors) {
      deleteResult(anchor)
    }
  }

  private def deleteResult(delResult: Range) {

    //If the result has already been deleted or has been added by the added results.
    if (additionResults.contains(delResult) || deletionResults.contains(delResult)) {
      return
      //Delete the result and continue deleteing recursively.
    } else {
      deletionResults.add(delResult)
      notify_removed(delResult)

      var it: java.util.Iterator[Domain] = deletions.get(rangeKeyFunction(delResult)).iterator()
      while (it.hasNext) {
        val next: Domain = it.next()
        val nextResult: Range = step(next, delResult)
        deleteResult(nextResult)
      }
    }
  }



  override def endTransaction() {
    doRecursionForAddedElements()
    doRecursionForRemovedElements()
    clear()
    notify_endTransaction()

  }

  override def clear() {
    additionAnchors = Nil
    deletionAnchors = Nil
    additionResults = mutable.HashSet.empty[Range]
    deletionResults = mutable.HashSet.empty[Range]
    // TODO remove any data structures you define.
    // please store them as "var" and do,  x = new HashMap, or something
    super.clear()
  }

  override def added(v: Domain) {
    //println("added -> I was here!")
    val anchor = anchorFunction(v)
    if (anchor.isDefined && !additionResults.contains(anchor.get)) {
      additionAnchors = anchor.get :: additionAnchors
      //element_added(anchor.get)
      //additionResults.add(anchor.get)
    }
    super.added(v)
  }

  override def removed(v: Domain) {
    val anchor = anchorFunction(v)
    if (anchor.isDefined && !deletionResults.contains(anchor.get)) {
      deletionAnchors = anchor.get :: deletionAnchors
      //element_removed(anchor.get)
      //deletionResults.add(anchor.get)
    }
    super.removed(v)
  }


  def foreach[T](f: (Range) => T) {
    /* do nothing, since this is a transactional view */
	  throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
  }
}