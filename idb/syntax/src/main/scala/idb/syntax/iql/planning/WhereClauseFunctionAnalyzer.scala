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

import scala.virtualization.lms.internal.{GraphTraversal, Effects, Expressions}
import idb.lms.extensions.ExpressionUtils
import scala.virtualization.lms.common.{EqualExpOpt, BooleanOpsExp}

/**
 *
 * @author Ralf Mitschke
 */
trait WhereClauseFunctionAnalyzer extends GraphTraversal
{

    val IR: Expressions with ExpressionUtils with Effects with BooleanOpsExp with EqualExpOpt = idb.syntax.iql.IR

    import IR._


    private def isBooleanAnd (e: Exp[Any]): Boolean =
        e match {
            case Def (BooleanAnd (_, _)) => true
            case _ => false
        }

    private def isEquality (e: Exp[Any]): Boolean =
        e match {
            case Def (Equal (_, _)) => true
            case _ => false
        }


    /**
     * Finds all filters functions (any function to boolean) and equalities (tests depedning on two vars) in a
     * boolean expression.
     * The vars for which to search are given as parameter.
     * @param body
     * @param vars
     * @return A tuple of maps for (filters, equalities). Where each map contains sets of variables that are used in
     *         the expressions as keys and a list of conjunctive expressions as value.
     */
    def filtersAndEqualities (
        body: Exp[Boolean],
        vars: List[Exp[Any]]
    ): (Map[Set[Exp[Any]], Exp[Boolean]], Map[Set[Exp[Any]], Exp[Boolean]]) = {
        val (filtersPartition, equalitiesPartition) = partitionFiltersAndEqualities (body)

        var varsInFilters = categorizeByUsedSubExpressions (filtersPartition, vars)

        var varsInEqualities = categorizeByUsedSubExpressions (equalitiesPartition, vars)

        for (key <- varsInEqualities.keys.filter (_.size == 1)) {
            val oldFilterExpr = varsInFilters.getOrElse (key, Nil)
            val newFilterExpr = oldFilterExpr ::: varsInEqualities (key)
            varsInFilters += (key -> newFilterExpr)
            varsInEqualities -= key
        }

        val filters = varsInFilters.mapValues (_.reduceLeft (boolean_and))

        val equalities = varsInEqualities.mapValues (_.reduceLeft (boolean_and))

        (filters, equalities)
    }


    private def partitionFiltersAndEqualities (body: Exp[Boolean]): (List[Exp[Boolean]], List[Exp[Boolean]]) = {
        val conjuncts = splitExpression (body, isBooleanAnd)
        conjuncts.partition (!isEquality (_))
    }

    private def splitExpression[Result: Manifest] (
        body: Exp[Result],
        canSplit: Exp[Result] => Boolean
    ): List[Exp[Result]] = {

        var result: List[Exp[Result]] = Nil

        def traversalFun (e: Exp[Result]): Boolean = {
            if (canSplit (e)) {
                return true
            }
            result = e :: result
            false
        }

        traverseSameTypeExpTree (body)(traversalFun)
        result
    }

    private def categorizeByUsedSubExpressions[T: Manifest] (
        expressions: List[Exp[T]],
        subExpressions: List[Exp[Any]]
    ): Map[Set[Exp[Any]], List[Exp[T]]] = {
        var result = Map.empty[Set[Exp[Any]], List[Exp[T]]]
        implicit val searchedSyms = subExpressions.toSet
        for (exp <- expressions) {
            val category: Set[Exp[Any]] = findSyms (exp).asInstanceOf[Set[Exp[Any]]]
            val oldInCategory = result.getOrElse (category, Nil)
            result += (category -> (exp :: oldInCategory))
        }

        result
    }

}
