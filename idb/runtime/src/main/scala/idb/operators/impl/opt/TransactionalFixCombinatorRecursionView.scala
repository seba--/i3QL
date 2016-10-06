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
import idb.{View, Relation}
import idb.observer.{Observer, NotifyObservers}
import idb.operators.impl.util.TransactionKeyValueObserver

/**
 *
 *
 * @author Ralf Mitschke
 */
class TransactionalFixCombinatorRecursionView[Domain, Range, Key](val anchors: Relation[Range],
                                                                  val source: Relation[Domain],
                                                                  val domainKeyFunction: Domain => Key,
                                                                  val rangeKeyFunction: Range => Key,
                                                                  val combinationFunction: (Range, Range) => Range,
                                                                  val step: (Domain, Range) => Range,
                                                                  override val isSet: Boolean)
  extends View[Range]
  with TransactionKeyValueObserver[Key, Domain]
  with NotifyObservers[Range] {

  anchors.addObserver(AnchorObserver)

  source addObserver this


  var additionAnchors: List[Range] = Nil
  var additionResults = mutable.HashMap.empty[Key, Range]
  var deletionAnchors: List[Range] = Nil
  var deletionResults = mutable.HashMap.empty[Key, Range]


  def keyFunc = domainKeyFunction

  override def children() = List(anchors, source)

  override def lazyInitialize() {

  }

  override protected def resetInternal(): Unit = {
    clear()
  }

  private var recursionStack: List[List[Range]] = Nil

  def doRecursionForAddedElements() {
    val stackBase =
      for {anchor <- additionAnchors
           key = rangeKeyFunction(anchor)
           if !additionResults.contains(key)
      } yield {
        additionResults(key) = anchor
        notify_added(anchor)
        anchor
      }

    if (stackBase.isEmpty) {
      return
    }

    recursionStack = List(stackBase)

    while (!recursionStack.isEmpty) {

      //println (recursionStack.size)
      //println (additionResults.size)
      // we have derived base and now we want to derive further values recursively
      val currentResult = recursionStack.head.head
      // remove the current value from the current level
      recursionStack = recursionStack.head.tail :: recursionStack.tail


      var it: java.util.Iterator[Domain] = additions.get(rangeKeyFunction(currentResult)).iterator()
      var nextResults: List[Range] = Nil
      while (it.hasNext) {
        val joinedElement: Domain = it.next()
        val nextResult: Range = step(joinedElement, currentResult)
        val key = rangeKeyFunction(nextResult)

        if (additionResults.contains(key)) {
          val oldResult = additionResults(key)
          val combinedResult = combinationFunction(oldResult, nextResult)
          if (oldResult != combinedResult) {
            notify_removed(oldResult)
            // add elements of the next level
            notify_added(combinedResult)
            nextResults = combinedResult :: nextResults
            additionResults(key) = (combinedResult)
          }
        }
        else {
          additionResults(key) = (nextResult)
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
    val stackBase =
      for {anchor <- deletionAnchors
           key = rangeKeyFunction(anchor)
           if !deletionResults.contains(key)
      } yield {
        deletionResults(key) = anchor
        notify_removed(anchor)
        anchor
      }

    if (stackBase.isEmpty) {
      return
    }

    recursionStack = List(stackBase)

    while (!recursionStack.isEmpty) {

      //println (recursionStack.size)
      //println (additionResults.size)
      // we have derived base and now we want to derive further values recursively
      val currentResult = recursionStack.head.head
      // remove the current value from the current level
      recursionStack = recursionStack.head.tail :: recursionStack.tail


      var it: java.util.Iterator[Domain] = deletions.get(rangeKeyFunction(currentResult)).iterator()
      var nextResults: List[Range] = Nil
      while (it.hasNext) {
        val joinedElement: Domain = it.next()
        val nextResult: Range = step(joinedElement, currentResult)
        val key = rangeKeyFunction(nextResult)

        if (deletionResults.contains(key)) {
          val oldResult = deletionResults(key)
          val combinedResult = combinationFunction(oldResult, nextResult)
          if (oldResult != combinedResult) {
            //element_added(oldResult)
            // add elements of the next level
            //element_removed (combinedResult)
            nextResults = combinedResult :: nextResults
            deletionResults(key) = (combinedResult)
          }
        }
        else {
          deletionResults(key) = (nextResult)
          // add elements of the next level
          //element_removed (nextResult)
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

    deletionResults.values.foreach(
      notify_removed
    )
  }


  override def endTransaction() {
    sourcesTransactionEnded = true
    if (!anchorsTransactionEnded) {
      return
    }


    doRecursionForAddedElements()
    doRecursionForRemovedElements()
    clear()
    anchorsTransactionEnded = false
    sourcesTransactionEnded = false
    notify_endTransaction()

  }

  override def clear() {
    additionAnchors = Nil
    deletionAnchors = Nil
    additionResults = mutable.HashMap.empty[Key, Range]
    deletionResults = mutable.HashMap.empty[Key, Range]

    super.clear()
  }


  def foreach[T](f: (Range) => T) {
    /* do nothing, since this is a transactional view */
    throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
  }

  var anchorsTransactionEnded = false

  var sourcesTransactionEnded = false

  object AnchorObserver extends Observer[Range] {


    def added(v: Range) {
      additionAnchors = v :: additionAnchors
    }

    def addedAll(vs: Seq[Range]) {
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
      sourcesTransactionEnded = false

      notify_endTransaction()
    }

    def updated(oldV: Range, newV: Range) {
      throw new UnsupportedOperationException
    }
  }

  override def prettyprint(implicit prefix: String) = prefix +
    s"TransactionalFixCombinatorRecursionView(${nested(source)})"

}