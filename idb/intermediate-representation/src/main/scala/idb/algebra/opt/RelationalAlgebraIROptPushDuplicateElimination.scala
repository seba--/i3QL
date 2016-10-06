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
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.functions.FunctionsExpDynamicLambda
import idb.query.QueryEnvironment
import scala.virtualization.lms.common._

/**
 * Simplification rules remove operators that reduce to trivial meanings.
 * For example: a ∩ a = a
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptPushDuplicateElimination
    extends RelationalAlgebraIRBasicOperators
    with BaseFatExp
    with TupledFunctionsExp
    with FunctionsExpDynamicLambda
    with FunctionUtils
{


    override def duplicateElimination[Domain: Manifest] (
        relation: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        (relation match {

            // δ (Π {(x, y) => x} (a × b)) => δ (a)
            // δ (Π {(x, y) => y} (a × b)) => δ (b)
            case Def (Projection (Def (cross: CrossProduct[Any@unchecked, Any@unchecked]), f)) =>
                // TODO, why can I not pattern match the cross product above as CrossProduct(a,b)?
                val bodyIsParameterAtIndex = returnedParameter (f)
                bodyIsParameterAtIndex match {
                    case -1 => super.duplicateElimination (relation)
                    case 0 => duplicateElimination (cross.relationA)(domainOf (cross.relationA), queryEnvironment)
                    case 1 => duplicateElimination (cross.relationB)(domainOf (cross.relationB), queryEnvironment)
                    case _ =>
                        throw new IllegalStateException (
                            "Expected a binary function as projection after cross product, " +
                                "but found more parameters" + f.toString)
                }


            // δ (Π {(x, y) => x} (a {xa => ...} ⋈ {xb => ...} b)) =>
            //   Π {(x, y) => x} (δ (a) {xa => ...} ⋈ {xb => xb} δ (Π {xb => ...} (b)))
            // δ (Π {(x, y) => y} (a {xa => ...} ⋈ {xb => ...} b)) =>
            //   Π {(x, y) => y} (δ (Π {xa => ...} (a)) {xa => xa} ⋈ {xb => ...} δ (b))
            // these rules are primarily being used to simplify expressions after generating exists sub queries
            case Def (Projection (Def (join: EquiJoin[Any@unchecked, Any@unchecked]), f))
                // TODO, why can I not pattern match the equi join above as EquiJoin(a,b,list)?
                if join.equalities.size < 6 => // we can only convert this using tupled functions of size 5

		        val bodyIsParameterAtIndex = returnedParameter (f)

                bodyIsParameterAtIndex match {
                    case -1 =>
                        super.duplicateElimination (relation)
                    case 0 =>
                        // if the first parameter is returned by the projection, this cast is legitimate
                        val relationA = join.relationA.asInstanceOf[Rep[Query[Domain]]]
                        val relationB = join.relationB
                        val equalities = join.equalities.asInstanceOf[List[(Rep[Domain => Any], Rep[Any => Any])]]
                        val innerProjection = convertRightEqualitiesToProjectedTuple (equalities)
                        val equalityProjection = convertLeftEqualitiesToProjectedTuple (equalities)

                        val newJoin =
                            equiJoin (
                                duplicateElimination (relationA),
                                duplicateElimination (
                                    projection (
                                        relationB,
                                        innerProjection
                                    )
                                ),
                                scala.List ((equalityProjection, fun ((x: Rep[Any]) => x)))
                            )(manifest[Domain], returnType (innerProjection), queryEnvironment)

                        projection (
                            newJoin,
                            (p: Rep[(Domain, Any)]) => p._1
                        )

                    case 1 =>
                        val relationA = join.relationA
                        // if the second parameter is returned by the projection, this cast is legitimate
                        val relationB = join.relationB.asInstanceOf[Rep[Query[Domain]]]
                        val equalities = join.equalities.asInstanceOf[List[(Rep[Any => Any], Rep[Domain => Any])]]
                        val innerProjection = convertLeftEqualitiesToProjectedTuple (equalities)
                        val equalityProjection = convertRightEqualitiesToProjectedTuple (equalities)

                        val newJoin =
                            equiJoin (
                                duplicateElimination (
                                    projection (
                                        relationA,
                                        innerProjection
                                    )
                                ),
                                duplicateElimination (relationB),
                                scala.List ((fun ((x: Rep[Any]) => x), equalityProjection))
                            )(returnType (innerProjection), manifest[Domain], queryEnvironment)

                        projection (
                            newJoin,
                            (p: Rep[(Any, Domain)]) => p._2
                        )

                    case _ =>
                        throw new IllegalStateException (
                            "Expected a binary function as projection after equi join, " +
                                "but found more parameters" + f.toString)
                }


            case _ => super.duplicateElimination (relation)

        }).asInstanceOf[Rep[Query[Domain]]]


    // takes a list of equalities, selects the first equality function and
    // converts the selected functions into a projection from the Domain to a tuple
    private def convertLeftEqualitiesToProjectedTuple[DomainA, DomainB] (
        equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): Rep[DomainA => Any] = {
        implicit val mA = parameterType (equalities (0)._1).asInstanceOf[Manifest[DomainA]]
        equalities.map (_._1) match {
            case scala.List (f) => f

            case scala.List (f1, f2) =>
                fun ((x: Rep[DomainA]) => make_tuple2(f1 (x), f2 (x)))(mA,
                    tupledManifest (returnType (f1), returnType (f2))
                )

            case scala.List (f1, f2, f3) =>
                fun ((x: Rep[DomainA]) => (f1 (x), f2 (x), f3 (x)))(mA,
                    tupledManifest (returnType (f1), returnType (f2), returnType (f3))
                )

            case scala.List (f1, f2, f3, f4) =>
                fun ((x: Rep[DomainA]) => (f1 (x), f2 (x), f3 (x), f4 (x)))(mA,
                    tupledManifest (returnType (f1), returnType (f2), returnType (f3), returnType (f4))
                )

            case scala.List (f1, f2, f3, f4, f5) =>
                fun ((x: Rep[DomainA]) => (f1 (x), f2 (x), f3 (x), f4 (x), f5 (x)))(mA,
                    tupledManifest (returnType (f1), returnType (f2), returnType (f3), returnType (f4), returnType (f5))
                )

            case _ => throw new UnsupportedOperationException
        }
    }

    // takes a list of equalities, selects the second equality function and
    // converts the selected functions into a projection from the Domain to a tuple
    private def convertRightEqualitiesToProjectedTuple[DomainA, DomainB] (
        equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): Rep[DomainB => Any] = {
        implicit val mB = parameterType (equalities (0)._2).asInstanceOf[Manifest[DomainB]]
        equalities.map (_._2) match {
            case scala.List (f) => f

            case scala.List (f1, f2) =>
                fun ((x: Rep[DomainB]) => make_tuple2(f1 (x), f2 (x)))(mB,
                    tupledManifest (returnType (f1), returnType (f2))
                )

            case scala.List (f1, f2, f3) =>
                fun ((x: Rep[DomainB]) => (f1 (x), f2 (x), f3 (x)))(mB,
                    tupledManifest (returnType (f1), returnType (f2), returnType (f3))
                )

            case scala.List (f1, f2, f3, f4) =>
                fun ((x: Rep[DomainB]) => (f1 (x), f2 (x), f3 (x), f4 (x)))(mB,
                    tupledManifest (returnType (f1), returnType (f2), returnType (f3), returnType (f4))
                )

            case scala.List (f1, f2, f3, f4, f5) =>
                fun ((x: Rep[DomainB]) => (f1 (x), f2 (x), f3 (x), f4 (x), f5 (x)))(mB,
                    tupledManifest (returnType (f1), returnType (f2), returnType (f3), returnType (f4), returnType (f5))
                )

            case _ => throw new UnsupportedOperationException
        }
    }

}


