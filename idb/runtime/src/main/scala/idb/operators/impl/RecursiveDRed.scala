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

import idb.Relation
import idb.operators.Recursive
import idb.observer.NotifyObservers
import scala.collection.mutable


/**
 *
 * @author Ralf Mitschke
 *
 */

class RecursiveDRed[Domain](val relation: Relation[Domain],
                            override val isSet: Boolean,
                            val transactional: Boolean = false
                             )
  extends Recursive[Domain]
  with NotifyObservers[Domain] {

  relation.addObserver(this)

  // the set of elements in the current recursion
  // recording them avoids endless recursions
  //
  // the recursive path taken to support the currently added value
  private var currentSupportPath: List[Seq[Domain]] = Nil

  // these elements were already derived once and will not be propagated a second time
  private var supportedElements: mutable.HashMap[Domain, Int] = mutable.HashMap.empty

  private var deletedElements: mutable.HashMap[Domain, Int] = mutable.HashMap.empty

  private def mergeCurrentSupportTo(v: Domain) {
    val supportingPathCount = supportedElements(v)
    supportedElements(v) = supportingPathCount + 1
  }


  private def deleteCurrentSupportTo(v: Domain) {
    val supportingPathCount = deletedElements(v)
    deletedElements(v) = supportingPathCount + 1
  }


  /**
   * recursion stacks will look lick this
   * Nil
   * List(Nil)
   * List(List(v), Nil)
   * List(List(v), List(u), Nil)
   * List(List(v), List(u,w), Nil)
   * List(List(v), List(u), Nil)
   * List(List(v), List(u), Nil)
   * List(List(v), Nil)
   * List(Nil)
   * Nil
   */
  private var additionRecursionStack: List[Seq[Domain]] = Nil


  private var deletionRecursionStack: List[Seq[Domain]] = Nil

  // delete supports
  private var rederivations: Seq[Domain] = Seq()

  override protected[idb] def resetInternal(): Unit = ???

  def added(v: Domain) {
    addedAll(Seq(v))
  }

  def addedAll(vs: Seq[Domain]) {
//    println(s"Recursive add, size ${vs.size}")

    val newVs = vs filter {v =>
      if (supportedElements.contains(v)) {
        // we have reached a value that was previously defined.

        // add the current recursion to the support for v
        mergeCurrentSupportTo(v)
        false
      }
      else {
        supportedElements(v) = 1
        true
      }
    }

    if (newVs.isEmpty)
      return

    if (!additionRecursionStack.isEmpty) {
      // we have derived a new value in the recursion
      // it will be treated in the enclosing call

      // basically everything that ends up here comes from one single
      // call to element_added, i.e., it is all derived at the same recursion depth

      // add v as a derived value at the current recursion depth
      additionRecursionStack = (additionRecursionStack.head ++ newVs) :: additionRecursionStack.tail
    }
    else {
      // we have reached the start of a recursion

      additionRecursionStack = List(newVs)
      while (!additionRecursionStack.isEmpty) {
        // we have derived next and now we want to derive further values recursively
        val nexts = additionRecursionStack.head
        // remove the current value from the current level
        additionRecursionStack = Seq() :: additionRecursionStack.tail
        // add a new empty list at the beginning of the level
        additionRecursionStack = Seq() :: additionRecursionStack
        // next is a support on the current recursive path
        currentSupportPath = nexts :: currentSupportPath
        // add elements of the next level
        //println("Added:  " + next)
        notify_addedAll(nexts)

        // we did not compute a new level, i.e., the next recursion level of values is empty
        // remove all empty levels
        while (!additionRecursionStack.isEmpty && additionRecursionStack.head.isEmpty) {
          additionRecursionStack = additionRecursionStack.tail
          if (!currentSupportPath.isEmpty) {
            currentSupportPath = currentSupportPath.tail
          }
        }
      }
    }
  }

  def removed(v: Domain) {
    removedAll(Seq(v))
  }

  def removedAll(vs: Seq[Domain]) {
//    println(s"Recursive rem, size ${vs.size}")

    val newVs = vs filter { v =>
      if (deletedElements.contains(v)) {
        // we have reached a value that was previously defined.

        // add the current recursion to the support for v
        deleteCurrentSupportTo(v)
        false
      }
      else {
        deletedElements(v) = 1
        true
      }
    }

    if (newVs.isEmpty)
      return

    if (!deletionRecursionStack.isEmpty) {
      // we have derived a new value in the recursion
      // it will be treated in the enclosing call

      // basically everything that ends up here comes from one single
      // call to element_added, i.e., it is all derived at the same recursion depth

      // add v as a derived value at the current recursion depth
      deletionRecursionStack = (deletionRecursionStack.head ++ newVs) :: deletionRecursionStack.tail
      return
    }
    else {
      // we have reached the start of a recursion

      deletionRecursionStack = List(newVs)

      while (!deletionRecursionStack.isEmpty) {
        // we have derived next and now we want to derive further values recursively
        val nexts = deletionRecursionStack.head
        // remove the current value from the current level
        deletionRecursionStack = Seq() :: deletionRecursionStack.tail
        // add a new empty list at the beginning of the level
        deletionRecursionStack = Seq() :: deletionRecursionStack
        // next is a support on the current recursive path
        currentSupportPath = nexts :: currentSupportPath
        // add elements of the next level
        //println("Remove: " + next)
        notify_removedAll(nexts)

        // we did not compute a new level, i.e., the next recursion level of values is empty
        // remove all empty levels
        while (!deletionRecursionStack.isEmpty && deletionRecursionStack.head.isEmpty) {
          deletionRecursionStack = deletionRecursionStack.tail
          if (!currentSupportPath.isEmpty) {
            currentSupportPath = currentSupportPath.tail
          }
        }
      }

      for ((key, deletionCount) <- deletedElements) {
        val newSupportCount = supportedElements(key) - deletionCount
        if (newSupportCount > 0) {
          supportedElements(key) = newSupportCount
          rederivations = key +: rederivations
        }
        else {
          supportedElements.remove(key)
        }
      }

      deletedElements = mutable.HashMap.empty

      //println("Rederive: " + rederivations)
      notify_addedAll(rederivations)
      rederivations = Seq()
    }
  }

  def updated(oldV: Domain, newV: Domain) {
    if (!additionRecursionStack.isEmpty) {
      // we are currently adding elements
      supportedElements(oldV) = supportedElements(oldV) - 1
      added(newV)
    }
    else if (!deletionRecursionStack.isEmpty) {
      // we are currently deleting elements
      removed(oldV)
      supportedElements(newV) = supportedElements.getOrElse(newV, 0) + 1
      rederivations = newV +: rederivations
    }
    else {
      removed(oldV)
      added(newV)
    }
  }
}

