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
package sae.operators.impl

import sae.operators.RecursiveView
import sae.deltas.{Update, Deletion, Addition}
import sae.Relation
import collection.mutable

/**
 *
 * @author Ralf Mitschke
 *
 */

class RecursiveDRed[Domain](val relation: Relation[Domain])
    extends RecursiveView[Domain]
{
    relation.addObserver (this)

    // the set of elements in the current recursion
    // recording them avoids endless recursions
    //
    // the recursive path taken to support the currently added value
    private var currentSupportPath: List[Domain] = Nil

    // these elements were already derived once and will not be propagated a second time
    private val supportedElements: mutable.HashMap[Domain, List[List[Domain]]] = mutable.HashMap.empty


    private def mergeCurrentSupportTo(v: Domain) {
        val supportingPaths = supportedElements (v)
        supportingPaths.foreach (
            path => {
                if (path == currentSupportPath) {
                    return
                }
            }
        )
        supportedElements (v) = currentSupportPath :: supportingPaths
    }


    private def deleteCurrentSupportTo(v: Domain) {
        val supportingPaths = deletedElements (v)
        supportingPaths.foreach (
            path => {
                if (path == currentSupportPath) {
                    return
                }
            }
        )
        deletedElements (v) = currentSupportPath :: supportingPaths
    }

    private val deletedElements: mutable.HashMap[Domain, List[List[Domain]]] = mutable.HashMap.empty

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
    private var recursionStack: List[List[Domain]] = Nil


    def added(v: Domain) {
        if (supportedElements.contains (v)) {
            // we have reached a value that was previously defined.

            // add the current recursion to the support for v
            mergeCurrentSupportTo (v)
            return
        }
        else
        {
            supportedElements (v) = List (currentSupportPath)
        }

        if (!recursionStack.isEmpty) {
            // we have derived a new value in the recursion
            // it will be treated in the enclosing call

            // basically everything that ends up here comes from one single
            // call to element_added, i.e., it is all derived at the same recursion depth

            // add v as a derived value at the current recursion depth
            recursionStack = (v :: recursionStack.head) :: recursionStack.tail
            return
        }


        // we have reached the start of a recursion

        recursionStack = List (List (v))
        while (!recursionStack.isEmpty) {
            // we have derived next and now we want to derive further values recursively
            val next = recursionStack.head.head
            // remove the current value from the current level
            recursionStack = recursionStack.head.tail :: recursionStack.tail
            // add a new empty list at the beginning of the level
            recursionStack = Nil :: recursionStack
            // next is a support on the current recursive path
            currentSupportPath = next :: currentSupportPath
            // add elements of the next level
            element_added (next)

            // we did not compute a new level, i.e., the next recursion level of values is empty
            // remove all empty levels
            while (!recursionStack.isEmpty && recursionStack.head == Nil) {
                recursionStack = recursionStack.tail
                if (!currentSupportPath.isEmpty) {
                    currentSupportPath = currentSupportPath.tail
                }
            }
        }
    }

    def removed(v: Domain) {
        if (deletedElements.contains (v)) {
            // we have reached a value that was previously defined.

            // add the current recursion to the support for v
            deleteCurrentSupportTo (v)
            return
        }
        else
        {
            deletedElements (v) = List (currentSupportPath)
        }

        if (!recursionStack.isEmpty) {
            // we have derived a new value in the recursion
            // it will be treated in the enclosing call

            // basically everything that ends up here comes from one single
            // call to element_added, i.e., it is all derived at the same recursion depth

            // add v as a derived value at the current recursion depth
            recursionStack = (v :: recursionStack.head) :: recursionStack.tail
            return
        }


        // we have reached the start of a recursion

        recursionStack = List (List (v))
        while (!recursionStack.isEmpty) {
            // we have derived next and now we want to derive further values recursively
            val next = recursionStack.head.head
            // remove the current value from the current level
            recursionStack = recursionStack.head.tail :: recursionStack.tail
            // add a new empty list at the beginning of the level
            recursionStack = Nil :: recursionStack
            // next is a support on the current recursive path
            currentSupportPath = next :: currentSupportPath
            // add elements of the next level
            element_removed (next)

            // we did not compute a new level, i.e., the next recursion level of values is empty
            // remove all empty levels
            while (!recursionStack.isEmpty && recursionStack.head == Nil) {
                recursionStack = recursionStack.tail
                if (!currentSupportPath.isEmpty) {
                    currentSupportPath = currentSupportPath.tail
                }
            }
        }
    }

    def updated(oldV: Domain, newV: Domain) {
        throw new UnsupportedOperationException
    }

    def updated[U <: Domain](update: Update[U]) {
        throw new UnsupportedOperationException
    }

    def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        throw new UnsupportedOperationException
    }

}
