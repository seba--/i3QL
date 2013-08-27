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
import sae.operators.impl.{AggregationForNotSelfMaintainableFunctions, WITH_RECURSIVE, EquiJoinView, RecursiveDRed}
import sae.SetExtent
import sae.operators.{NotSelfMaintainableAggregateFunctionFactory, NotSelfMaintainableAggregateFunction}

/**
 *
 * @author Ralf Mitschke
 *
 */

class DRed
{

    case class Instruction(index:Int)

    case class StateInfo(instruction:Instruction, value:String)

    case class ControlFlowEdge(current:Instruction, next:Instruction)

    def nextState : (ControlFlowEdge, StateInfo) => StateInfo = null

    def combineStates : (StateInfo, StateInfo) => StateInfo = null

    @Test
    def testStateChange() {

        val startInstructions = new SetExtent[StateInfo]

        val controlFlow = new SetExtent[ControlFlowEdge]

        val startStates =
            new RecursiveDRed (
                startInstructions
            )

        val states = WITH_RECURSIVE (
            startStates,
            new AggregationForNotSelfMaintainableFunctions(
                new EquiJoinView (
                    controlFlow,
                    startStates,
                    (_: ControlFlowEdge).current,
                    (_: StateInfo).instruction,
                    nextState
                ),
                (_: StateInfo).instruction,
                CombineStates,
                (i: Instruction, s: StateInfo) => s
            )
        ).named ("dataflow").asMaterialized


        startStates.element_added(StateInfo(Instruction(0), "start"))
    }

    private class CombineStateFunction
        extends NotSelfMaintainableAggregateFunction[StateInfo, StateInfo]
    {
        var currentState: StateInfo = null

        def add(newD: StateInfo, data: Iterable[StateInfo]): StateInfo = {
            if (currentState == null) {
                currentState = newD
            }
            else
            {
                currentState = combineStates (currentState, newD)
            }
            currentState
        }

        def remove(newD: StateInfo, data: Iterable[StateInfo]): StateInfo = {
            if (data.isEmpty) {
                currentState = StateInfo (newD.instruction, "start")
            }
            else
            {
                currentState = data.reduce (combineStates)
            }
            currentState
        }

        def update(oldD: StateInfo, newD: StateInfo, data: Iterable[StateInfo]): StateInfo = {
            throw new UnsupportedOperationException
        }
    }

    private object CombineStates extends NotSelfMaintainableAggregateFunctionFactory[StateInfo, StateInfo]
    {
        def apply(): NotSelfMaintainableAggregateFunction[StateInfo, StateInfo] = {
            new CombineStateFunction
        }
    }

}
