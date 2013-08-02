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

import idb.syntax.iql.impl._
import idb.lms.extensions.FunctionCreator

/**
 *
 * @author Ralf Mitschke
 *
 */

trait PlanWhereClause
    //extends FunctionCreator
    //with WhereClauseFunctionAnalyzer
{

    val IR = idb.syntax.iql.IR

    import IR._


    def transform[Domain: Manifest, Range: Manifest] (
        clause: WhereClause1[_, Domain, Range],
        relation: Rep[Query[Domain]]
    ): Rep[Query[Domain]] =
        selection (relation, clause.predicate)


    def transform[DomainA: Manifest, DomainB: Manifest, Range: Manifest] (
        clause: WhereClause2[_, _, DomainA, DomainB, Range],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): Rep[Query[(DomainA, DomainB)]] =
        selection (crossProduct (relationA, relationB), clause.predicate)

    //buildPredicateOperators(clause.predicate, relationA, relationB)


    def transform[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Range: Manifest] (
        clause: WhereClause3[DomainA, DomainB, DomainC, Range],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]]
    ): Rep[Query[(DomainA, DomainB, DomainC)]] =
        selection (crossProduct (relationA, relationB, relationC), clause.predicate)

    //buildPredicateOperators(clause.predicate, relationA, relationB, relationC)


    def transform[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        clause: WhereClause4[DomainA, DomainB, DomainC, DomainD, Range],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]]
    ): Rep[Query[(DomainA, DomainB, DomainC, DomainD)]] =
        selection (crossProduct (relationA, relationB, relationC, relationD), clause.predicate)

    //buildPredicateOperators(clause.predicate, relationA, relationB, relationC, relationD)


    def transform[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        clause: WhereClause5[DomainA, DomainB, DomainC, DomainD, DomainE, Range],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]],
        relationE: Rep[Query[DomainE]]
    ): Rep[Query[(DomainA, DomainB, DomainC, DomainD, DomainE)]] =
        selection (crossProduct (relationA, relationB, relationC, relationD, relationE), clause.predicate)

    //buildPredicateOperators(clause.predicate, relationA, relationB, relationC, relationD, relationE)

/*
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

    private def buildPredicateOperators[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest,
    DomainE: Manifest] (
        predicate: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD], Rep[DomainE]) => Rep[Boolean],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]],
        relationE: Rep[Query[DomainE]]
    ): Rep[Query[(DomainA, DomainB, DomainC, DomainD, DomainE)]] = {
        val a = fresh[DomainA]
        val b = fresh[DomainB]
        val c = fresh[DomainC]
        val d = fresh[DomainD]
        val e = fresh[DomainE]
        val vars = scala.List (a, b, c, d, e)

        val body = predicate (a, b, c, d, e)

        val (filters, equalities) = filtersAndEqualities (body, vars)

        buildPredicateOperators5 (relationA, relationB, relationC, relationD, relationE, a, b, c, d, e, filters,
            equalities)
    }


    private def buildPredicateOperators2[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        a: Exp[DomainA],
        b: Exp[DomainB],
        filters: Map[Set[Exp[Any]], Exp[Boolean]],
        equalities: Map[Set[Exp[Any]], List[Exp[Boolean]]]
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
                val splitedEqualities = splitJoinEqualities (scala.List (a), scala.List (b), equalities (keyAB))
                val equalityFunctions =
                    splitedEqualities.map (split =>
                        (fun (recreateFun (a, split._1)), fun (recreateFun (b, split._2)))
                    )
                equiJoin (relA, relB, equalityFunctions)
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
        equalities: Map[Set[Exp[Any]], List[Exp[Boolean]]]
    ): Rep[Query[(DomainA, DomainB, DomainC)]] = {
        val keyC = Predef.Set (c).asInstanceOf[Set[Exp[Any]]]
        val keyAC = Predef.Set (a, c)
        val keyBC = Predef.Set (b, c)
        val keyABC = Predef.Set (a, b, c)

        val lastRelation =
            if (filters.contains (keyC)) {
                selection (relationC, recreateFun (c, filters (keyC)))
            } else
            {
                relationC
            }

        val headRelation =
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
                val splitedEqualities =
                    splitJoinEqualities (scala.List (a, b), scala.List (c),
                        equalities.getOrElse (keyAC, Nil) ::: equalities.getOrElse (keyBC, Nil))
                val equalityFunctions =
                    splitedEqualities.map (split =>
                        (fun (recreateFun ((a, b), split._1)), fun (recreateFun (c, split._2)))
                    )

                equiJoin (headRelation, lastRelation, equalityFunctions)
            } else
            {
                crossProduct (headRelation, lastRelation)
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
        equalities: Map[Set[Exp[Any]], List[Exp[Boolean]]]
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
                val splitedEqualities =
                    splitJoinEqualities (scala.List (a, b, c), scala.List (d),
                        equalities.getOrElse (keyAD, Nil) ::: equalities.getOrElse (keyBD, Nil) ::: equalities
                            .getOrElse (keyCD, Nil))
                val equalityFunctions =
                    splitedEqualities.map (split =>
                        (fun (recreateFun ((a, b, c), split._1)), fun (recreateFun (d, split._2)))
                    )

                equiJoin (headRelation, lastRelation, equalityFunctions)
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

    private def buildPredicateOperators5[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest,
    DomainE: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]],
        relationE: Rep[Query[DomainE]],
        a: Exp[DomainA],
        b: Exp[DomainB],
        c: Exp[DomainC],
        d: Exp[DomainD],
        e: Exp[DomainE],
        filters: Map[Set[Exp[Any]], Exp[Boolean]],
        equalities: Map[Set[Exp[Any]], List[Exp[Boolean]]]
    ): Rep[Query[(DomainA, DomainB, DomainC, DomainD, DomainE)]] = {
        val keyE = Predef.Set (d).asInstanceOf[Set[Exp[Any]]]
        val keyAE = Predef.Set (a, e)
        val keyBE = Predef.Set (b, e)
        val keyCE = Predef.Set (c, e)
        val keyDE = Predef.Set (d, e)
        val keyABCDE = Predef.Set (a, b, c, d, e)

        val lastRelation =
            if (filters.contains (keyE)) {
                selection (relationE, recreateFun (e, filters (keyE)))
            } else
            {
                relationE
            }

        val headRelation =
            buildPredicateOperators4 (
                relationA,
                relationB,
                relationC,
                relationD,
                a,
                b,
                c,
                d,
                filters,
                equalities
            )

        val join =
            if (equalities.contains (keyAE) || equalities.contains (keyBE) || equalities.contains (keyCE) || equalities
                .contains (keyDE))
            {
                val splitedEqualities =
                    splitJoinEqualities (scala.List (a, b, c), scala.List (d),
                        equalities.getOrElse (keyAE, Nil) ::: equalities.getOrElse (keyBE, Nil) ::: equalities
                            .getOrElse (keyCE, Nil) ::: equalities.getOrElse (keyDE, Nil))
                val equalityFunctions =
                    splitedEqualities.map (split =>
                        (fun (recreateFun ((a, b, c, d), split._1)), fun (recreateFun (e, split._2)))
                    )

                equiJoin (headRelation, lastRelation, equalityFunctions)
            } else
            {
                crossProduct (headRelation, lastRelation)
            }

        val normalizedJoin = projection (join, flattenTuple5 (_: Rep[((DomainA, DomainB, DomainC, DomainD), DomainE)]))

        val upperSelect =
            if (filters.contains (keyABCDE)) {
                selection (normalizedJoin, recreateFun ((a, b, c, d, e), filters (keyABCDE)))
            } else
            {
                normalizedJoin
            }

        upperSelect
    }
*/
}
