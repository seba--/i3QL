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
package idb.functions

import com.google.common.collect.{SortedMultiset, TreeMultiset}
import idb.operators.{SelfMaintainableAggregateFunction, SelfMaintainableAggregateFunctionFactory}

/**
 *
 * @author Ralf Mitschke
 *
 */

object Sort
{
    def apply[Domain](implicit ordering: Ordering[Domain]): SelfMaintainableAggregateFunctionFactory[Domain, SortedMultiset[Domain]] = {
        new SelfMaintainableAggregateFunctionFactory[Domain, SortedMultiset[Domain]]
        {
            def apply(): SelfMaintainableAggregateFunction[Domain, SortedMultiset[Domain]] = {
                new DirectSort[Domain](ordering)
            }
        }
    }

    def apply[Domain, T](f: Domain => T)(implicit ordering: Ordering[T]): SelfMaintainableAggregateFunctionFactory[Domain, SortedMultiset[T]] = {
        new SelfMaintainableAggregateFunctionFactory[Domain, SortedMultiset[T]]
        {
            def apply(): SelfMaintainableAggregateFunction[Domain, SortedMultiset[T]] = {
                new TransformedSort[Domain, T](f, ordering)
            }
        }
    }
}

private class DirectSort[Domain](val ordering: Ordering[Domain])
    extends SelfMaintainableAggregateFunction[Domain, SortedMultiset[Domain]]
{
    val data: TreeMultiset[Domain] = TreeMultiset.create[Domain](ordering)

    def add(d: Domain) = {
        data.add (d)
        data
    }

    def remove(d: Domain) = {
        data.remove (d)
        data
    }

    def update(oldV: Domain, newV: Domain) = {
        val count = data.count (oldV)
        data.remove (oldV, count)
        data.add (newV, count)
        data
    }

	def get = data
}


private class TransformedSort[Domain, T] (val f: Domain => T, val ordering: Ordering[T])
    extends SelfMaintainableAggregateFunction[Domain, SortedMultiset[T]]
{
    val data: TreeMultiset[T] = TreeMultiset.create[T](ordering)

    def add(d: Domain) = {
        data.add (f (d))
        data
    }

    def remove(d: Domain) = {
        data.remove (f (d))
        data
    }

    def update(oldV: Domain, newV: Domain) = {
        val count = data.count (f (oldV))
        data.remove (f (oldV), count)
        data.add (f (newV), count)
        data
    }

	def get = data
}