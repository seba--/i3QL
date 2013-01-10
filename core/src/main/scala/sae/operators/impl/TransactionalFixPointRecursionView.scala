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

import sae.operators.FixPointRecursion
import util.TransactionKeyValueObserver
import sae.Relation
import collection.mutable

/**
 *
 *
 * @author Ralf Mitschke
 */
class TransactionalFixPointRecursionView[Domain, Range, Key](val source: Relation[Domain],
                                                             val anchorFunction: Domain => Option[Range],
                                                             val domainKeyFunction: Domain => Key,
                                                             val rangeKeyFunction: Range => Key,
                                                             val step: (Domain, Range) => Range)
        extends FixPointRecursion[Domain, Range, Key]
        with TransactionKeyValueObserver[Key, Domain]
{

    var additionAnchors: List[Range] = Nil

    var additionElements = mutable.HashSet.empty[Range]

    var deletionsAnchors: List[Range] = Nil

    var deletionElements = mutable.HashSet.empty[Range]


    def keyFunc = domainKeyFunction

    def doRecursionForAddedElements() {
        // TODO compute the recursive values

        // all domain values are stored in the Multimap "additions"
        for (anchor <- additionAnchors) {

            // TODO do something like this recursively!!
            // It has to be done recursively, since for each new element you can have multiple matching domain values
            // hence you need a recursive call to retain the domain values at which you "forked" the computation
            // you could try to optimize this by checking whether "additions.get(key).size() == 1" and in this case just doing a while loop
            val key = rangeKeyFunction(anchor)

            var it: java.util.Iterator[Domain] = additions.get(key).iterator()
            while (it.hasNext) {
                val value = it.next()
                var nextElement = step(value, anchor)
                if (!additionElements.containsEntry(nextElement)) {
                    element_added(nextElement)

                }

            }

        }
    }

    def doRecursionForRemovedElements() {
        // TODO compute the recursive values

        // all domain values are stored in the Multimap "deletions"
    }

    override def endTransaction() {
        doRecursionForAddedElements()
        doRecursionForRemovedElements()
        super.endTransaction()
    }

    override def clear() {
        additionAnchors = Nil
        deletionsAnchors = Nil
        additionElements = mutable.HashSet.empty[Range]
        deletionElements = mutable.HashSet.empty[Range]
        // TODO remove any data structures you define.
        // please store them as "var" and do,  x = new HashMap, or something
        super.clear()
    }

    override def added(v: Domain) {
        val anchor = anchorFunction(v)
        if (anchor.isDefined && !additionElements.contains(anchor.get)) {
            additionAnchors = anchor.get :: additionAnchors
            element_added(anchor.get)
            additionElements.add(anchor.get)
        }
        super.added(v)
    }

    override def removed(v: Domain) {
        val anchor = anchorFunction(v)
        if (anchor.isDefined && !deletionElements.contains(anchor.get)) {
            deletionsAnchors = anchor.get :: deletionsAnchors
            element_removed(anchor.get)
        }
        super.removed(v)
    }


    def foreach[T](f: (Range) => T) {
        /* do nothing, since this is a transactional view */
    }

    /**
     * Returns true if there is some intermediary storage, i.e., foreach is guaranteed to return a set of values.
     */
    def isStored = false

}