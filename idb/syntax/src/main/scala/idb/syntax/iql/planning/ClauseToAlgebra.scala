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
package idb.syntax.iql.planning

//import idb.syntax.iql.IR._

import idb.syntax.iql._
import scala.virtualization.lms.common.ForwardTransformer
import idb.syntax.iql.impl._
import idb.lms.extensions.FunctionBodies

/**
 *
 * @author Ralf Mitschke
 *
 */

object ClauseToAlgebra
    extends ForwardTransformer
    with FunctionBodies
    with WhereClauseFunctionAnalyzer
{
    override val IR = idb.syntax.iql.IR

    import IR._

    def apply[Domain: Manifest, Range: Manifest] (
        query: IQL_QUERY_1[Domain, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause1 (relation, SelectClause1 (project)) =>
                projection (relation, project)

            case WhereClause1 (predicate, FromClause1 (relation, SelectClause1 (project))) =>
                projection (selection (relation, predicate), project)

        }

    def apply[DomainA: Manifest, DomainB: Manifest, Range: Manifest] (
        query: IQL_QUERY_2[DomainA, DomainB, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause2 (relationA, relationB, SelectClause2 (project)) =>
                projection (crossProduct (relationA, relationB), project)

            case WhereClause2 (predicate, FromClause2 (relationA, relationB, SelectClause2 (project))) => {
                projection (buildPredicateOperators (predicate, relationA, relationB), project)
            }
        }

    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Range: Manifest] (
        query: IQL_QUERY_3[DomainA, DomainB, DomainC, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause3 (relationA, relationB, relationC, SelectClause3 (project)) =>
                projection (crossProduct (relationA, relationB, relationC), project)

            case WhereClause3 (predicate, FromClause3 (relationA, relationB, relationC, SelectClause3 (project))) => {
                projection (buildPredicateOperators (predicate, relationA, relationB, relationC), project)
            }
        }

    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        query: IQL_QUERY_4[DomainA, DomainB, DomainC, DomainD, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause4 (relationA, relationB, relationC, relationD, SelectClause4 (project)) =>
                projection (crossProduct (relationA, relationB, relationC, relationD), project)

            case WhereClause4 (predicate,
            FromClause4 (relationA, relationB, relationC, relationD, SelectClause4 (project))) =>
            {
                projection (buildPredicateOperators (predicate, relationA, relationB, relationC, relationD), project)
            }
        }

    private def buildPredicateOperators[DomainA: Manifest, DomainB: Manifest] (
        predicate: (Rep[DomainA], Rep[DomainB]) => Rep[Boolean],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): Rep[Query[(DomainA, DomainB)]] = {
        val a = fresh[DomainA]
        val b = fresh[DomainB]
        val vars = scala.List (a, b)

        val body = predicate (a, b)

        val (filters, equalities) = filtersAndEqualities (body, vars)

        buildPredicateOperators2 (relationA, relationB, a, b, filters, equalities)
    }

    private def buildPredicateOperators[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest] (
        predicate: (Rep[DomainA], Rep[DomainB], Rep[DomainC]) => Rep[Boolean],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]]
    ): Rep[Query[(DomainA, DomainB, DomainC)]] = {
        val a = fresh[DomainA]
        val b = fresh[DomainB]
        val c = fresh[DomainC]
        val vars = scala.List (a, b, c)

        val body = predicate (a, b, c)

        val (filters, equalities) = filtersAndEqualities (body, vars)

        buildPredicateOperators3 (relationA, relationB, relationC, a, b, c, filters, equalities)
    }

    private def buildPredicateOperators[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest] (
        predicate: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD]) => Rep[Boolean],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]]
    ): Rep[Query[(DomainA, DomainB, DomainC, DomainD)]] = {
        val a = fresh[DomainA]
        val b = fresh[DomainB]
        val c = fresh[DomainC]
        val d = fresh[DomainD]
        val vars = scala.List (a, b, c, d)

        val body = predicate (a, b, c, d)

        val (filters, equalities) = filtersAndEqualities (body, vars)

        buildPredicateOperators4 (relationA, relationB, relationC, relationD, a, b, c, d, filters, equalities)
    }


    private def buildPredicateOperators2[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        a: Exp[DomainA],
        b: Exp[DomainB],
        filters: Map[Set[Exp[Any]], Exp[Boolean]],
        equalities: Map[Set[Exp[Any]], Exp[Boolean]]
    ): Rep[Query[(DomainA, DomainB)]] = {
        val keyA = Predef.Set (a).asInstanceOf[Set[Exp[Any]]]
        val keyB = Predef.Set (b).asInstanceOf[Set[Exp[Any]]]
        val keyAB = Predef.Set (a, b)

        val relA =
            if (filters.contains (keyA)) {
                selection (relationA, recreateFun (a, filters (keyA)))
            } else
            {
                relationA
            }

        val relB =
            if (filters.contains (keyB)) {
                selection (relationB, recreateFun (b, filters (keyB)))
            } else
            {
                relationB
            }

        val join =
            if (equalities.contains (keyAB)) {
                equiJoin (relA, relB, null)
            } else
            {
                crossProduct (relA, relB)
            }

        val upperSelect =
            if (filters.contains (keyAB)) {
                selection (join, recreateFun ((a, b), filters (keyAB)))
            } else
            {
                join
            }

        upperSelect

    }

    private def buildPredicateOperators3[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        a: Exp[DomainA],
        b: Exp[DomainB],
        c: Exp[DomainC],
        filters: Map[Set[Exp[Any]], Exp[Boolean]],
        equalities: Map[Set[Exp[Any]], Exp[Boolean]]
    ): Rep[Query[(DomainA, DomainB, DomainC)]] = {
        val keyC = Predef.Set (c).asInstanceOf[Set[Exp[Any]]]
        val keyAC = Predef.Set (a, c)
        val keyBC = Predef.Set (b, c)
        val keyABC = Predef.Set (a, b, c)

        val relC =
            if (filters.contains (keyC)) {
                selection (relationC, recreateFun (c, filters (keyC)))
            } else
            {
                relationC
            }

        val relAB =
            buildPredicateOperators2 (
                relationA,
                relationB,
                a,
                b,
                filters,
                equalities
            )

        val join =
            if (equalities.contains (keyAC) || equalities.contains (keyBC)) {
                equiJoin (relAB, relC, null)
            } else
            {
                crossProduct (relAB, relC)
            }

        val normalizedJoin = projection (join, flattenTuple3 (_: Rep[((DomainA, DomainB), DomainC)]))

        val upperSelect =
            if (filters.contains (keyABC)) {
                selection (normalizedJoin, recreateFun ((a, b, c), filters (keyABC)))
            } else
            {
                normalizedJoin
            }

        upperSelect
    }

    private def buildPredicateOperators4[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]],
        a: Exp[DomainA],
        b: Exp[DomainB],
        c: Exp[DomainC],
        d: Exp[DomainD],
        filters: Map[Set[Exp[Any]], Exp[Boolean]],
        equalities: Map[Set[Exp[Any]], Exp[Boolean]]
    ): Rep[Query[(DomainA, DomainB, DomainC, DomainD)]] = {
        val keyD = Predef.Set (d).asInstanceOf[Set[Exp[Any]]]
        val keyAD = Predef.Set (a, d)
        val keyBD = Predef.Set (b, d)
        val keyCD = Predef.Set (c, d)
        val keyABCD = Predef.Set (a, b, c, d)

        val lastRelation =
            if (filters.contains (keyD)) {
                selection (relationD, recreateFun (d, filters (keyD)))
            } else
            {
                relationD
            }

        val headRelation =
            buildPredicateOperators3 (
                relationA,
                relationB,
                relationC,
                a,
                b,
                c,
                filters,
                equalities
            )

        val join =
            if (equalities.contains (keyAD) || equalities.contains (keyBD) || equalities.contains (keyCD)) {
                equiJoin (headRelation, lastRelation, null)
            } else
            {
                crossProduct (headRelation, lastRelation)
            }

        val normalizedJoin = projection (join, flattenTuple4 (_: Rep[((DomainA, DomainB, DomainC), DomainD)]))

        val upperSelect =
            if (filters.contains (keyABCD)) {
                selection (normalizedJoin, recreateFun ((a, b, c, d), filters (keyABCD)))
            } else
            {
                normalizedJoin
            }

        upperSelect
    }


    /*
        private def combineTupleFunction[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Result: Manifest] (
            f: Rep[(DomainA, DomainB, DomainC)] => Rep[Result]
        ): Rep[((DomainA, DomainB), DomainC)] => Rep[Result] = {
            (args: Rep[((DomainA, DomainB), DomainC)]) => f (args._1._1, args._1._2, args._2)
        }

        private def flattenTupleFunction[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Result: Manifest] (
            f: Rep[((DomainA, DomainB), DomainC)] => Rep[Result]
        ): Rep[(DomainA, DomainB, DomainC)] => Rep[Result] = {
            (args: Rep[(DomainA, DomainB, DomainC)]) => {
                val combinedTuple: Rep[((DomainA, DomainB), DomainC)] =
                    make_tuple2 (make_tuple2 (args._1, args._2), args._3)
                //((args._1, args._2), args._3)
                f (combinedTuple)
            }

        }
       */

    /*
    private def predicateOperators2[DomainA: Manifest, DomainB: Manifest] (
        predicate: (Rep[DomainA], Rep[DomainB]) => Rep[Boolean],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): Rep[Query[(DomainA, DomainB)]] = {
        val a = fresh[DomainA]
        val b = fresh[DomainB]
        val body = predicate (a, b)
        implicit val searchParams: Set[Exp[Any]] = Predef.Set (a, b)

        val functionBodies = predicatesForTwoRelations (a, b, body)

        val relA = if (functionBodies.b1.isDefined) {
            selection (relationA, recreateFun (functionBodies.x1, functionBodies.b1.get))
        } else
        {
            relationA
        }

        val relB = if (functionBodies.b2.isDefined) {
            selection (relationB, recreateFun (functionBodies.x2, functionBodies.b2.get))
        } else
        {
            relationB
        }

        val join = if (functionBodies.b3.isDefined) {
            equiJoin (relA, relB, createJoinFunctions (a, b, functionBodies.b3.get))
        } else
        {
            crossProduct (relA, relB)
        }

        val upperSelect = if (functionBodies.b4.isDefined) {
            selection (join, recreateFun (functionBodies.x4, functionBodies.b4.get))
        } else
        {
            join
        }

        upperSelect
    }
    */

    /**
     * Returns an object containing method bodies for the where clause using two relations.
     * All method bodies are boolean predicates
     * The order of the bodies defines predicates for:
     * 1. left relation
     * 2. right relation
     * 3. both relations
     * 4. join conditions
     */
    /*
    private def predicatesForTwoRelations[DomainA: Manifest, DomainB: Manifest] (
        a: Sym[DomainA],
        b: Sym[DomainB],
        body: Rep[Boolean]
    )(
        implicit allParams: Set[Exp[Any]]
    ): FunctionBodies4[DomainA, DomainB,
        (DomainA, DomainB), (DomainA, DomainB),
        Boolean] = {
        val sa: Predef.Set[Sym[Any]] = Predef.Set (a)
        val sb: Predef.Set[Sym[Any]] = Predef.Set (b)
        findSyms (body) match {
            case `sa` =>
                return FunctionBodies4 (a, Some (body), b, None, (a, b), None, (a, b), None)
            case `sb` =>
                return FunctionBodies4 (a, None, b, Some (body), (a, b), None, (a, b), None)
            case _ => // do nothing
        }

        // there are multiple parameter syms yet in the body
        body match {
            // match conjunctions that can be used before a join
            case Def (BooleanAnd (lhs, rhs)) =>
                predicatesForTwoRelations (a, b, lhs)
                    .combineWith (boolean_and)(predicatesForTwoRelations (a, b, rhs))(asUnique = false)
            // match a join condition
            case Def (Equal (lhs, rhs))
                if {
                    val ls = findSyms (lhs)
                    val rs = findSyms (rhs)
                    ls.size == 1 &&
                        rs.size == 1 &&
                        ls != rs
                } =>
                FunctionBodies4 (a, None, b, None, (a, b), Some (body), (a, b), None)
            // match a combined condition on the result
            case _ =>
                FunctionBodies4 (a, None, b, None, (a, b), None, (a, b), Some (body))
        }
    }
    */

    /*
    private def createJoinFunctions[DomainA: Manifest, DomainB: Manifest] (
        a: Sym[DomainA],
        b: Sym[DomainB],
        body: Rep[Boolean]
    )(
        implicit allParams: Set[Exp[Any]]
    ): Seq[(Rep[DomainA => Any], Rep[DomainB => Any])] = {
        val sa: Predef.Set[Sym[Any]] = Predef.Set (a)
        val sb: Predef.Set[Sym[Any]] = Predef.Set (b)
        body match {
            // match conjunctions of joins
            case Def (BooleanAnd (lhs, rhs)) =>
                createJoinFunctions (a, b, lhs) ++ createJoinFunctions (a, b, rhs)
            // match a join condition
            case Def (Equal (lhs, rhs)) =>
                val ls = findSyms (lhs)
                val rs = findSyms (rhs)
                assert (ls.size == 1 && rs.size == 1 && ls != rs)
                if (ls == sa && rs == sb) {
                    scala.Seq ((recreateFun (a, lhs), recreateFun (b, rhs)))
                }
                else
                {
                    scala.Seq ((recreateFun (a, rhs), recreateFun (b, lhs)))
                }
            case _ =>
                throw new IllegalStateException (
                    "trying to create join conditions with operators other than 'AND' or 'EQUALS'"
                )
        }
    }
    */
}
