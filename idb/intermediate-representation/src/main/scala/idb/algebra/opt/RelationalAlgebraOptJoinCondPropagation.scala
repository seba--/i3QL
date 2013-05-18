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
package idb.algebra.opt

import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.ExpressionUtils
import scala.virtualization.lms.common._

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraOptJoinCondPropagation
    extends RelationalAlgebraIRBasicOperators
    with FunctionsExp
    with BooleanOpsExp
    with ExpressionUtils
    with FilterUtils
{


    override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        equalities: Seq[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): Rep[Query[(DomainA, DomainB)]] = {
        val newLhsFilter = constructLhsFilters (relationA, relationB, equalities)
        val newRhsFilter = constructRhsFilters (relationA, relationB, equalities)


        val relA = relationA match {
            case Def (Selection (r, _)) if newLhsFilter.b1.isDefined => {
                selection (r, transformer.recreateFun (newLhsFilter.x1, newLhsFilter.b1.get))
            }
            case _ if newLhsFilter.b1.isDefined =>
                selection (relationA, transformer.recreateFun (newLhsFilter.x1, newLhsFilter.b1.get))

            case _ if newLhsFilter.b1.isEmpty =>
                relationA
        }


        val relB = relationB match {
            case Def (Selection (r, f)) if newRhsFilter.b1.isDefined => {
                selection (r, transformer.recreateFun (newRhsFilter.x1, newRhsFilter.b1.get))
            }
            case _ if newRhsFilter.b1.isDefined =>
                selection (relationB, transformer.recreateFun (newRhsFilter.x1, newRhsFilter.b1.get))

            case _ if newRhsFilter.b1.isEmpty =>
                relationB
        }

        super.equiJoin (relA, relB, equalities)
    }


}
