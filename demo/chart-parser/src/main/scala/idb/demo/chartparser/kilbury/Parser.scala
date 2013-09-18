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
package idb.demo.chartparser.kilbury

import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.SetExtent

/**
 *
 * @author Ralf Mitschke
 */
trait Parser
    extends Scanner
{

    val productions = SetExtent.empty[Production]()

    def topLevelCategory: Category

    val activeEdges: Rep[Query[Edge]] =
        SELECT (
            (p: Rep[Production], e: Rep[Edge]) => ActiveEdge (e.start, e.end, p.head, p.body.tail)
        ) FROM(productions.asMaterialized, passiveEdges) WHERE (
            (p: Rep[Production], e: Rep[Edge]) => p.body.head == e.category
            )

    val initialEdges: Rep[Query[Edge]] = passiveEdges UNION ALL (activeEdges)

    val combinedEdges: Rep[Query[Edge]] =
        WITH RECURSIVE (
            (edges: Rep[Query[Edge]]) =>
                initialEdges UNION ALL (
                    SELECT ((active: Rep[Edge], passive: Rep[Edge]) =>
                        ActiveEdge (active.start, passive.end, active.category, active.next.tail)

                    ) FROM(edges, edges) WHERE ((active: Rep[Edge], other: Rep[Edge]) =>
                        other.isPassive AND
                            active.end == other.start AND
                            active.next.head == other.category
                        )
                )
            )

    val success: Rep[Query[Edge]] =
        SELECT (*) FROM combinedEdges WHERE (_.category == topLevelCategory)
}
