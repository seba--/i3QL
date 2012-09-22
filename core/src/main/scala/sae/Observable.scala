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
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
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
package sae

import collection.mutable.HashSet

trait Observable[V]
{

    protected[sae] var observers: HashSet[Observer[V]] = new HashSet[Observer[V]]()

    def addObserver(o: Observer[V]) {
        // sanity check that the assumption of never adding the same observer twice holds
        if(observers.contains(o))
        {
            throw new IllegalArgumentException("observer already present: " + o.toString)
        }
        observers.add(o)
    }

    def removeObserver(o: Observer[V]) {
        observers.remove(o)
    }

    def clearObservers() {
        observers = HashSet.empty
    }

    def hasObservers = {
        !observers.isEmpty
    }

    /**
     * remove all observers
     */
    def clearObserversForChildren(visitChild: Observable[_ <: AnyRef] => Boolean) {
        for (observable <- children) {
            // remove all observers for this observable
            for (observer <- childObservers(observable)) {
                observable.removeObserver(observer.asInstanceOf[Observer[Any]])
            }
            // check whether we want to visit the observable
            if (observable.observers.isEmpty && visitChild(observable)) {
                observable.clearObserversForChildren(visitChild)
            }
        }
    }

    protected def children: Seq[Observable[_ <: AnyRef]] = Nil

    protected def childObservers(o: Observable[_]): Seq[Observer[_]] = Nil

    // Notify methods to notify the observers
    def element_added(v: V) {
        observers.foreach(_.added(v))
    }

    def element_removed(v: V) {
        observers.foreach(_.removed(v))
    }

    def element_updated(oldV: V, newV: V) {
        observers.foreach(_.updated(oldV, newV))
    }

}

