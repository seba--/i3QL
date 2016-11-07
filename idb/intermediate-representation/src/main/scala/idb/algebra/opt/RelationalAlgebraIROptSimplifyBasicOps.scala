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

import idb.algebra.ir.{RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.equivalence.TupledFunctionsExpAlphaEquivalence
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.lms.extensions.print.QuoteFunction
import idb.query.QueryEnvironment

import scala.virtualization.lms.common.{TupledFunctionsExp, StaticDataExp, StructExp, ScalaOpsPkgExp}

/**
 * Simplification rules remove operators that reduce to trivial meanings.
 * For example: a ∩ a = a
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptSimplifyBasicOps
    extends RelationalAlgebraIRBasicOperators
    with TupledFunctionsExpAlphaEquivalence
    with FunctionUtils
    with ScalaOpsPkgExp
{

    /**
     * Remove equivalent functions. Note that this requires ordering the functions to ensure that equivalent functions
     * are on top of one another
     */
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        relation match {
            case Def (select@Selection (r, f)) if isEquivalent (function, f) => select
            case _ => super.selection (relation, function)
        }

    /**
     * Remove projection that use the identity function or only one argument
     */
    override def projection[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =
        (if (isIdentity (function)) {
            relation
        } else
        {
            // check if the projection ignores some values
            // these rules are primarily being used to simplify expressions after generating exists sub queries
            val bodyReturnsParameter = returnedParameter (function)


            bodyReturnsParameter match {
                // { (x,y) => x}
                case 0 =>
                    relation match {
                        case Def (EquiJoin (ra, Def (rb: EquiJoin[Any@unchecked, Any@unchecked]), equalities))
                            if isEqualLeftToRightLeft (equalities) =>
                            rb.relationA match {
                                case `ra` =>
                                    projection (
                                        equiJoin (
                                            ra,
                                            rb.relationB,
                                            rb.equalities
                                        )(domainOf (ra), domainOf (rb.relationB), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => x
                                        )(domainOf (ra), domainOf (rb.relationB), domainOf (ra))
                                    )

                                case Def (DuplicateElimination (`ra`)) =>
                                    projection (
                                        equiJoin (
                                            ra,
                                            rb.relationB,
                                            rb.equalities
                                        )(domainOf (ra), domainOf (rb.relationB), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => x
                                        )(domainOf (ra), domainOf (rb.relationB), domainOf (ra))
                                    )

                                case _ => super.projection (relation, function)
                            }

                        case Def (EquiJoin (ra, Def (rb: EquiJoin[Any@unchecked, Any@unchecked]), equalities))
                            if isEqualLeftToRightRight (equalities) =>
                            rb.relationB match {
                                case `ra` =>
                                    projection (
                                        equiJoin (
                                            ra,
                                            rb.relationA,
                                            rb.equalities
                                        )(domainOf (ra), domainOf (rb.relationA), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => x
                                        )(domainOf (ra), domainOf (rb.relationA), domainOf (ra))
                                    )

                                case Def (DuplicateElimination (`ra`)) =>
                                    projection (
                                        equiJoin (
                                            ra,
                                            rb.relationA,
                                            rb.equalities
                                        )(domainOf (ra), domainOf (rb.relationA), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => x
                                        )(domainOf (ra), domainOf (rb.relationA), domainOf (ra))
                                    )

                                case _ => super.projection (relation, function)
                            }


                        case _ =>
                            super.projection (relation, function)
                    }


                case 1 =>
                    relation match {
                        case Def (EquiJoin (Def (ra: EquiJoin[Any@unchecked, Any@unchecked]), rb, equalities))
                            if isEqualRightToLeftLeft (equalities) =>
                            ra.relationA match {
                                case `rb` =>
                                    projection (
                                        equiJoin (
                                            ra.relationB,
                                            rb,
                                            ra.equalities
                                        )(domainOf (ra.relationB), domainOf (rb), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => y
                                        )(domainOf (ra.relationB), domainOf (rb), domainOf (rb))
                                    )

                              	case Def (DuplicateElimination (`rb`)) =>
                                    projection (
                                        equiJoin (
                                            ra.relationB,
                                            rb,
                                            ra.equalities
                                        )(domainOf (ra.relationB), domainOf (rb), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => y
                                        )(domainOf (ra.relationB), domainOf (rb), domainOf (rb))
                                    )

                                case _ => super.projection (relation, function)
                            }

                        case Def (EquiJoin (Def (ra: EquiJoin[Any@unchecked, Any@unchecked]), rb, equalities))
                            if isEqualRightToLeftRight (equalities) =>
                            ra.relationB match {
                                case `rb` =>
                                    projection (
                                        equiJoin (
                                            ra.relationA,
                                            rb,
                                            ra.equalities
                                        )(domainOf (ra.relationA), domainOf (rb), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => y
                                        )(domainOf (ra.relationA), domainOf (rb), domainOf (rb))
                                    )

                                case Def (DuplicateElimination (`rb`)) =>
                                    projection (
                                        equiJoin (
                                            ra.relationA,
                                            rb,
                                            ra.equalities
                                        )(domainOf (ra.relationA), domainOf (rb), queryEnvironment),
                                        fun (
                                            (x: Rep[Any], y: Rep[Any]) => y
                                        )(domainOf (ra.relationA), domainOf (rb), domainOf (rb))
                                    )

                                case _ =>
                                    super.projection (relation, function)
                            }
						case _ => //TODO is this correct?
							super.projection(relation, function)
                    }

                case _ =>
                    super.projection (relation, function)
            }
        }).asInstanceOf[Rep[Query[Range]]]


    /**
     * Checks that the equality contains two functions of the form:
     * left  == x => x
     * right == x => x._1
     */
    private def isEqualLeftToRightLeft (equalities: List[(Rep[Any => Any], Rep[Any => Any])]): Boolean = {
        for ((left, right) <- equalities) {
            val leftIsIdentity = isIdentity (left)
            val rightReturnsLeftElement = returnsLeftOfTuple2 (right)
            if (leftIsIdentity && rightReturnsLeftElement) return true
        }

        false
    }

    /**
     * Checks that the equality contains two functions of the form:
     * left  == x => x
     * right == x => x._2
     */
    private def isEqualLeftToRightRight (equalities: List[(Rep[Any => Any], Rep[Any => Any])]): Boolean = {
        for ((left, right) <- equalities) {
            val leftIsIdentity = isIdentity (left)
            val rightReturnsRightElement = returnsRightOfTuple2 (right)
            if (leftIsIdentity && rightReturnsRightElement) return true
        }

        false
    }

    /**
     * Checks that the equality contains two functions of the form:
     * left  == x => x._1
     * right == x => x
     */
    private def isEqualRightToLeftLeft (equalities: List[(Rep[Any => Any], Rep[Any => Any])]): Boolean = {
        for ((left, right) <- equalities) {
            val rightIsIdentity = isIdentity (right)
            val leftReturnsLeftElement = returnsLeftOfTuple2 (right)
            if (rightIsIdentity && leftReturnsLeftElement) return true
        }

        false
    }

    /**
     * Checks that the equality contains two functions of the form:
     * left  == x => x
     * right == x => x._2
     */
    private def isEqualRightToLeftRight (equalities: List[(Rep[Any => Any], Rep[Any => Any])]): Boolean = {
        for ((left, right) <- equalities) {
            val rightIsIdentity = isIdentity (right)
            val leftReturnsRightElement = returnsRightOfTuple2 (left)
            if (rightIsIdentity && leftReturnsRightElement) return true
        }

        false
    }
}


