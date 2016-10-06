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
import idb.operators.impl.util.TransactionKeyValueObserver
import idb.observer.{Observer, NotifyObservers}

/**
 *
 *
 * @author Ralf Mitschke
 */
class TransactionalAnchorAndFixPointRecursionView[Domain, Range, Key](val anchors: Relation[Range],
                                                                      val source: Relation[Domain],
                                                                      val domainKeyFunction: Domain => Key,
                                                                      val rangeKeyFunction: Range => Key,
                                                                      val step: (Domain, Range) => Range)
  extends Relation[Range]
  with TransactionKeyValueObserver[Key, Domain]
  with NotifyObservers[Range] {

  anchors.addObserver(AnchorObserver)

  source addObserver this


  var additionAnchors: List[Range] = Nil
  var additionResults = mutable.HashSet.empty[Range]
  var deletionAnchors: List[Range] = Nil
  var deletionResults = mutable.HashSet.empty[Range]


  def keyFunc = domainKeyFunction

  def children() = List(anchors, source)

  override def lazyInitialize() {

  }

  override protected def resetInternal(): Unit = {
    clear()
  }

  private var recursionStack: List[List[Range]] = Nil

  def doRecursionForAddedElements() {
    var addedBase = Seq[Range]()
    val stackBase =
      for (anchor <- additionAnchors
           if !additionResults.contains(anchor)
      ) yield {
        additionResults.add(anchor)
        addedBase = anchor +: addedBase
        anchor
      }
    notify_addedAll(addedBase)

    if (stackBase.isEmpty) {
      return
    }

    recursionStack = List(stackBase)

    while (!recursionStack.isEmpty) {

      //println(recursionStack.size)
      println(recursionStack.size)
      println(additionResults.size)
      // we have derived base and now we want to derive further values recursively
      val currentResult = recursionStack.head.head
      // remove the current value from the current level
      recursionStack = recursionStack.head.tail :: recursionStack.tail


      var it: java.util.Iterator[Domain] = additions.get(rangeKeyFunction(currentResult)).iterator()
      var nextResults: List[Range] = Nil
      while (it.hasNext) {
        val joinedElement: Domain = it.next()
        val nextResult: Range = step(joinedElement, currentResult)
        if (!additionResults.contains(nextResult)) {
          additionResults.add(nextResult)
          // add elements of the next level
          notify_added(nextResult)
          nextResults = nextResult :: nextResults
        }
      }

      // add a the next Results at the beginning of the next level
      recursionStack = nextResults :: recursionStack


      // we did not compute a new level, i.e., the next recursion level of values is empty
      // remove all empty levels
      while (!recursionStack.isEmpty && recursionStack.head == Nil) {
        recursionStack = recursionStack.tail
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
    if (deletionResults.contains(delResult)) {
      return
      //Delete the result and continue deleting recursively.
    }
    else {
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
    sourcesTransactionEnded = true
    if (!anchorsTransactionEnded) {
      return
    }


    doRecursionForAddedElements()
    doRecursionForRemovedElements()
    clear()
    sourcesTransactionEnded = false
    notify_endTransaction()

  }

  override def clear() {
    additionAnchors = Nil
    deletionAnchors = Nil
    additionResults = mutable.HashSet.empty[Range]
    deletionResults = mutable.HashSet.empty[Range]
    // please store them as "var" and do,  x = new HashMap, or something
    super.clear()
  }


  def foreach[T](f: (Range) => T) {
    throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
  }

  /**
   * Returns true if there is some intermediary storage, i.e., foreach is guaranteed to return a set of values.
   */
  def isStored = false

  def isSet = false

  var anchorsTransactionEnded = false

  var sourcesTransactionEnded = false

  object AnchorObserver extends Observer[Range] {


    def added(v: Range) {
      additionAnchors = v :: additionAnchors
    }

    def addedAll(vs: Seq[Range]): Unit = {
      additionAnchors = additionAnchors ++ vs
    }

    def removed(v: Range) {
      deletionAnchors = v :: deletionAnchors
    }

    def removedAll(vs: Seq[Range]) {
      deletionAnchors = deletionAnchors ++ vs
    }

    override def endTransaction() {
      anchorsTransactionEnded = true
      if (!sourcesTransactionEnded) {
        return
      }

      doRecursionForRemovedElements()
      doRecursionForAddedElements()
      clear()

      anchorsTransactionEnded = false

      notify_endTransaction()
    }

    def updated(oldV: Range, newV: Range) {
      removed(oldV)
      added(newV)
    }
  }

  override def prettyprint(implicit prefix: String) = prefix +
    s"TransactionalAnchorAndFixPointRecursionView(${nested(source)})"

}