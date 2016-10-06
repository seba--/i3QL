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

import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.{MaterializedView, SetExtent}
import idb.operators.impl.CyclicTransitiveClosureView

/**
 *
 * @author Ralf Mitschke
 */
class ListOrderingInvariantAsTransitiveClosure
{

    private implicit def intListElemToOps (e: Rep[IntListElem]) =
        IntListElemOps (e)

    private case class IntListElemOps (e: Rep[IntListElem])
    {
        def value: Rep[Int] = field[Int](e, "value")

        def next: Rep[IntListElem] = field[IntListElem](e, "next")
    }


    val elements = SetExtent.empty[IntListElem]()


    val closure: MaterializedView[(IntListElem, IntListElem)] = new
            CyclicTransitiveClosureView[IntListElem, IntListElem](
                elements,
                new IntListElementAdjacencyList (),
                identity[IntListElem],
                (_: IntListElem).next,
                true
            )

    val chainedElements: Relation[(IntListElem, IntListElem)] =
        SELECT (*) FROM closure WHERE ((e: Rep[(IntListElem, IntListElem)]) => e._2 != nullToRepNull (null) AND e
            ._1 != nullToRepNull (null))

    val unorderedElements: MaterializedView[(IntListElem, IntListElem)] =
        (SELECT (*) FROM chainedElements WHERE ((e: Rep[(IntListElem, IntListElem)]) => e._1.value > e._2.value))
            .asMaterialized
}
