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
package idb


/**
 *
 *
 * A relation is the base trait for all operators and views.
 *
 * All relations can be iterated over.
 * The iteration requires that this relation or view is materialized, or one of the underlying relation is materialized.
 * If no relation whatsoever is materialized the iteration returns has no elements.
 *
 * All relations can be materialized, even if they do not store elements themselves.
 *
 *
 * @author Ralf Mitschke
 */


trait MaterializedView[+V]
    extends View[V]
{

    def contains[U >: V] (element: U): Boolean

    /**
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the value before the event.
     */
    def count[U >: V] (element: U): Int

    def foreachWithCount[T] (f: (V, Int) => T)

    /**
     * Returns the size of the view in terms of elements.
     * This can be a costly operation.
     * Implementors should cache the value in a self-maintained view, but clients can not rely on this.
     */
    def size: Int

    def isDefinedAt[U >: V] (v: U): Boolean = contains (v)


    /**
     * Returns the concrete element in this relation, with the most specific type
-    */
    //def elementAt[T >: V](v: T): V
}