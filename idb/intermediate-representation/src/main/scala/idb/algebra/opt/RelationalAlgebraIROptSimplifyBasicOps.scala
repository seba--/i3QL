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

import scala.virtualization.lms.common._
import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.FunctionUtils

/**
 * Simplification rules remove operators that reduce to trivial meanings.
 * For example: a ∩ a = a
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptSimplifyBasicOps
    extends RelationalAlgebraIRBasicOperators
    with TupledFunctionsExp
    with FunctionUtils
{

    /**
     * Remove projection that use the identity function
     */
    override def projection[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    ): Rep[Query[Range]] =
        if (isIdentity (function)) {
            relation.asInstanceOf[Rep[Query[Range]]]
        } else
        {
            // check if the projection ignores some values
            // these rules are primarily being used to simplify expressions after generating exists sub queries
            val bodyReturnsParameter = returnedParameter (function)

            bodyReturnsParameter match
            {
                // { (x,y) => x}
                case 0 =>
                    relation match {
                        case Def (EquiJoin (ra, rb, equalities)) if equalities.contains (isObjectEquality _) =>
                            super.projection (relation,function)
                        case _ =>
                            super.projection (relation,function)
                    }


                case 1 =>
                    super.projection (relation,function)

                case -1 => super.projection (relation,function)
            }
        }

/*
    // these rules are primarily being used to simplify expressions after generating exists sub queries
    override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): Rep[Query[(DomainA, DomainB)]] =
        if (equalities.contains (isObjectEquality _)) {
            // this join is a natural join, so we can try to find inner expressions containing relationA or relationB
            ((relationA, relationB) match {
                case (_, Def (Projection (Def (join: EquiJoin[Any@unchecked, Any@unchecked]), f))) =>
                    // TODO why can I not pattern match the equi join
                {
                    val bodyReturnsParameter = returnedParameter (f)

                    bodyReturnsParameter match
                    {
                        case 0 =>
                            join.relationA match
                            {
                                case `relationA` =>
                                    equiJoin (
                                        relationA,
                                        join.relationB,
                                        join.equalities
                                    )(exactDomainOf (relationA), domainOf (join.relationB))
                                case Def (DuplicateElimination (`relationA`)) =>
                                    equiJoin (
                                        relationA,
                                        join.relationB,
                                        join.equalities
                                    )(exactDomainOf (relationA), domainOf (join.relationB))
                            }

                        case 1 =>
                            super.equiJoin (relationA, relationB, equalities)

                        case -1 => super.equiJoin (relationA, relationB, equalities)
                    }
                }

                case _ => super.equiJoin (relationA, relationB, equalities)
            }).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]
        }
        else
            super.equiJoin (relationA, relationB, equalities)

*/
}


