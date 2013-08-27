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
package sae.test

import org.junit.Test
import org.junit.Assert._
import sae.operators.impl.{AggregationForNotSelfMaintainableFunctions, WITH_RECURSIVE, EquiJoinView, RecursiveDRed}
import sae.SetExtent
import sae.operators.{NotSelfMaintainableAggregateFunctionFactory, NotSelfMaintainableAggregateFunction}

/**
 *
 * @author Ralf Mitschke
 *
 */

class DRedUnion
{

    @Test
    def testStateChange() {

        val start = new SetExtent[Set[String]]

        val controlFlow = new SetExtent[String]

        val startStates =
            new RecursiveDRed (
                start
            )

        val states = WITH_RECURSIVE (
            startStates,
            new AggregationForNotSelfMaintainableFunctions(
                startStates,
                (_:Set[String]) => "a",
                CombineStates,
                (a:String, b:Set[String]) => b
            )
        ).asMaterialized


        start.element_added(Set("a"))

        assertEquals(List(Set("a")),states.asList)

        start.element_added(Set("b"))

        assertEquals(List(Set("a", "b")),states.asList)

        start.element_removed(Set("a"))

        assertEquals(List(Set("b")),states.asList)
    }

    private class CombineFunction
        extends NotSelfMaintainableAggregateFunction[Set[String], Set[String]]
    {
        def add(newD: Set[String], data: Iterable[Set[String]]) = data.reduce(_.union(_))

        def remove(newD: Set[String], data: Iterable[Set[String]]) = data.reduce(_.union(_))

        def update(oldD: Set[String], newD: Set[String], data: Iterable[Set[String]]) = data.reduce(_.union(_))
    }

    private object CombineStates extends NotSelfMaintainableAggregateFunctionFactory[Set[String], Set[String]]
    {
        def apply(): NotSelfMaintainableAggregateFunction[Set[String], Set[String]] = {
            new CombineFunction
        }
    }

}
