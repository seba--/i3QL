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


    def filtersAndEqualities (
        body: Exp[Boolean],
        vars: List[Exp[Any]]
    ): (Map[Set[Exp[Any]], Exp[Boolean]], Map[Set[Exp[Any]], Exp[Boolean]]) = {
        val (filtersPartition, equalitiesPartition) = partitionFiltersAndEqualities (body)

        var varsInFilters : Map[Set[Exp[Any]], List[Exp[Boolean]]]
        = categorizeByUsedSubExpressions (filtersPartition, vars)

        var varsInEqualities: Map[Set[Exp[Any]], List[Exp[Boolean]]]
        = categorizeByUsedSubExpressions (equalitiesPartition, vars)

        for (key <- varsInEqualities.keys.filter (_.size == 1)) {
            val oldFilterExpr = varsInFilters.getOrElse (key, Nil)
            val newFilterExpr = oldFilterExpr ::: varsInEqualities (key)
            varsInFilters += (key -> newFilterExpr)
            varsInEqualities -= key
        }

        val filters = varsInEqualities.mapValues (_.reduceLeft(boolean_and))

        val equalities = varsInEqualities.mapValues (_.reduceLeft(boolean_and))

        (filters, equalities)
    }


    private def partitionFiltersAndEqualities (body: Exp[Boolean]): (List[Exp[Boolean]], List[Exp[Boolean]]) = {
        val conjuncts = splitExpression (body, isBooleanAnd)
        /*
        conjuncts.foreach (
            _ match {
                case Def (s) => Predef.println (s)
                case e => Predef.println (e)
            }
        )
        */

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
            val category : Set[Exp[Any]] = findSyms (exp).asInstanceOf[Set[Exp[Any]]]
            val oldInCategory = result.getOrElse (category, Nil)
            result += (category -> (exp :: oldInCategory))
        }

        result
    }

    /**
     *
     * Split the expression into a tuple of expressions (x,y) such that
     * x reads at least one e in subExprs
     * y does not read any e in subExprs
     * y ad y are options that will be None if body falls only in one category
     */

    /*
    def splitExpression[Result: Manifest] (
        body: Exp[Result],
        subExprs: List[Exp[Any]],
        canSplit: Exp[Result] => Boolean,
        merge: (Exp[Result], Exp[Result]) => Exp[Result]
    ): (Option[Exp[Result]], Option[Exp[Result]]) = {
        var x: Option[Exp[Result]] = None
        var y: Option[Exp[Result]] = None

        def traversalFun (e: Exp[Result]): Boolean = {
            val readsSubExprs = !readSyms (e).intersect (subExprs).isEmpty
            if (readsSubExprs) {
                if (canSplit (e)) {
                    return true
                }
                x = Some (x.fold (e)(merge (_, e)))
                return false
            }

            y = Some (y.fold (e)(merge (_, e)))
            false
        }

        traverseSameTypeExpTree (body)(traversalFun)

        (x, y)
    }
    */

    //val IR: Expressions with Effects with BooleanOpsExp = idb.syntax.iql.IR

    //import IR._


    /*
    var split = Map.empty[Sym[_], Seq[Exp[_]]]

    override def traverseStm (stm: Stm) {
        println (stm)
        stm match {
            case TP (sym, rhs) => println (readSyms (rhs))
            case _ => // do nothing
        }

        //super.traverseStm (stm)
    }

    override def traverseStmsInBlock[A] (stms: List[Stm]) {
        super.traverseStmsInBlock (stms)
    }

    def splitStatements (stm: Stm) {

    }


    def doWork () {
        for (key <- split.keys) {
            println (getDependentStuff (key))
        }


    }


    def curryPredicates (vars: List[Sym[Any]], body: Exp[Any]): List[Sym[Any]] = {

    }

    def splitPredicates[A: Manifest, B: Manifest, R: Manifest] (
        f: Exp[(A, B) => R]
    ) : (Exp[A => R], Exp[B => R], Exp[(A, B) => R]) = {

    }


    /**
     *
     *
     * @return (left)
     */
    def splitExpression[Result: Manifest] (
        canSplit : Exp[Result] => Boolean,
        split : Exp[Result] => (Exp[Result], Exp[Result]),
        merge : (Exp[Result], Exp[Result]) =>  Exp[Result]
    )(
        body: Exp[Result]
    ) : (Option[Exp[Result]], Option[Exp[Result]], Option[Exp[Result]]) = {
        if(!canSplit(body)) {
            return (None, None, Some(body))
        }

        val subSplitCall =  splitExpression(canSplit, split, merge)_

        body match {
            case TP(_, rhs) => syms(rhs).foreach(subSplitCall(_))
            case Sym(_) => syms(body).foreach(subSplitCall(_))
        }
        val (lhs, rhs) = split(body)
        val leftSplit = splitExpression(canSplit, split, merge)(lhs)
        val rightSplit = splitExpression(canSplit, split, merge)(rhs)
        val first = leftSplit._1.flatMap( merge() )
    }

    def splitPredicates[A: Manifest, B: Manifest, C: Manifest] (
        canSplit : Exp[(A, B, C)] => Boolean,
        split : Exp[(A, B, C)] => (Exp[(A, B, C)], Exp[(A, B, C)])
    )(
        body: Exp[(A, B, C)],
        a : Sym[A],
        b : Sym[B],
        c : Sym[C]
    ) : (Option[Exp[(A, B)]], Option[Exp[C]], Option[Exp[(A, B, C)]]) = {
        if(!canSplit(body))
            return (None, None, Some(body))
        val (rhs, lhs) = split(body)


        val symsInF = readSyms(body)
        var usesA = false

        for ( sym <- symsInF) {
            sym == a
        }
        if( symsInF.contains(a) && symsInF.contains(b) &&)

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
}
