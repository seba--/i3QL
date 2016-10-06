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
import idb.algebra.print.RelationalAlgebraPrintPlan

/**
 *
 * @author Ralf Mitschke
 */
class ListOrderingInvariantAsRecursive
{

    val printer = new RelationalAlgebraPrintPlan {
        override val IR = idb.syntax.iql.IR
    }

    private implicit def intListElemToOps (e: Rep[IntListElem]) =
        IntListElemOps (e)

    private case class IntListElemOps (e: Rep[IntListElem])
    {
        def value: Rep[Int] = field[Int](e, "value")

        def next: Rep[IntListElem] = field[IntListElem](e, "next")
    }


    val elements = SetExtent.empty[IntListElem]()


    val chainedElements: Relation[IntListElem] =
        SELECT (*) FROM elements WHERE ((e: Rep[IntListElem]) => e.next != nullToRepNull (null))

    val unorderedChainedElements: Rep[Query[IntListElem]] =
        SELECT (*) FROM chainedElements WHERE ((e: Rep[IntListElem]) => e.value > e.next.value)

    val closure: Rep[Query[(IntListElem, IntListElem)]] =
        WITH RECURSIVE (
            (t: Rep[Query[(IntListElem, IntListElem)]]) =>
                SELECT ((e: Rep[IntListElem]) => (e, e.next)) FROM unorderedChainedElements
                    UNION ALL (
                    SELECT (
                        (head: Rep[IntListElem], edge: Rep[(IntListElem, IntListElem)]) => (head, edge._2)
                    ) FROM(chainedElements, t) WHERE (
                        (head: Rep[IntListElem], edge: Rep[(IntListElem, IntListElem)]) =>
                            head.next == edge._1
                        )
                )
            )

    Predef.println(printer.quoteRelation(closure))

    val unorderedElements: MaterializedView[(IntListElem, IntListElem)] = closure.asMaterialized
}
