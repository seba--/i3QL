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
package collection.incremental

import idb.Index

/**
 *
 * @author Ralf Mitschke
 */
class IntListElementAdjacencyList
    extends Index[IntListElem, IntListElem]
{
    def add_element (key: IntListElem, value: IntListElem) = throw new UnsupportedOperationException ()

    def contains (key: IntListElem): Boolean = key.next != null

    def count (key: IntListElem): Int = 1

    def foreachKey[U] (f: (IntListElem) => U) = throw new UnsupportedOperationException ()

    def get (key: IntListElem): Option[Traversable[IntListElem]] =
        if (contains (key)) {
            Some (List (key.next))
        } else
        {
            None
        }

    def keyFunction: (IntListElem) => IntListElem = throw new UnsupportedOperationException ()

    def put (key: IntListElem, value: IntListElem) = throw new UnsupportedOperationException ()

    def relation: idb.Relation[IntListElem] = throw new UnsupportedOperationException ()

    def remove_element (key: IntListElem, value: IntListElem) = throw new UnsupportedOperationException ()

    def size: Int = throw new UnsupportedOperationException ()

    def update_element (oldKey: IntListElem, oldV: IntListElem, newKey: IntListElem, newV: IntListElem) = throw new
            UnsupportedOperationException ()

    def foreach[T] (f: ((IntListElem, IntListElem)) => T) = throw new UnsupportedOperationException ()

    def isSet: Boolean = throw new UnsupportedOperationException ()
}