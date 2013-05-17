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
import idb.lms.extensions.{FunctionBodies, ExpressionUtils}

/**
 *
 * @author Ralf Mitschke
 */
trait FilterUtils
    extends RelationalAlgebraIRBasicOperators
    with FunctionsExp
    with BooleanOps
    with ExpressionUtils
{
    val transformer: FunctionBodies {val IR: FilterUtils.this.type}

    import transformer.FunctionBodies1

    def constructRhsFilters[DomainA: Manifest, DomainB: Manifest] (
        relation: Rep[Query[DomainA]],
        equalities: Seq[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): FunctionBodies1[DomainB, Boolean] = {
        relation match {
            case Def (Selection (r, Def (Lambda (_, a: Exp[DomainA], block: Block[Boolean])))) =>
                (for ((lhs, Def (Lambda (_, b: Exp[DomainB], Block (rhs)))) <- equalities) yield
                {
                    val lhsSubst = lhs (a)
                    transformer.subst = Map (lhsSubst -> rhs)
                    val res = transformer.transformBlock (block).res
                    implicit val search = Predef.Set (a, b)
                    if (findSyms (res) == Predef.Set (b))
                        FunctionBodies1 (b, Some (res))
                    else
                        FunctionBodies1 (b, None)(manifest[DomainB], manifest[Boolean])
                }).reduce (_.combineWith (transformer.IR.boolean_and)(_))
            case _ => FunctionBodies1 (fresh[DomainB], None)(manifest[DomainB], manifest[Boolean])
        }
    }

    def constructLhsFilters[DomainA: Manifest, DomainB: Manifest] (
        relation: Rep[Query[DomainB]],
        equalities: Seq[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): FunctionBodies1[DomainA, Boolean] = {
        relation match {
            case Def (Selection (r, Def (Lambda (_, b: Exp[DomainB], block: Block[Boolean])))) =>
                (for ((Def (Lambda (_, a: Exp[DomainA], Block (lhs))), rhs) <- equalities) yield
                {
                    val rhsSubst = rhs (b)
                    transformer.subst = Map (rhsSubst -> lhs)
                    val res = transformer.transformBlock (block).res
                    implicit val search = Predef.Set (a, b)
                    if (findSyms (res) == Predef.Set (a))
                        FunctionBodies1 (a, Some (res))
                    else
                        FunctionBodies1 (a, None)(manifest[DomainA], manifest[Boolean])
                }).reduce (_.combineWith (transformer.IR.boolean_and)(_))
            case _ => FunctionBodies1 (fresh[DomainA], None)(manifest[DomainA], manifest[Boolean])
        }
    }


}
