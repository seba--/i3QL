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
package idb.lms.extensions

import scala.virtualization.lms.internal.Expressions
import scala.virtualization.lms.common._


/**
 *
 * @author Ralf Mitschke
 */
trait ExpressionUtils
    extends Expressions
    with TupledFunctionsExp
    with EqualExpBridge
{


    /**
     * Traverse an expression tree and apply f to all elements.
     * Any (sub) expression where f returns true is traversed further.
     * Otherwise traversal stops
     * @param e expression to traverse
     * @param f function to apply to expressions and determining sub expression traversal
	 * @return False, if the traversal was stopped by f. True otherwise.
     */
    def traverseExpTree (e: Exp[Any])(implicit f: Exp[Any] => Boolean) {
        if (!f (e))
            return

        e match {
            case Def (s) =>
                val subExpr = syms (s)
                subExpr.foreach (traverseExpTree)

            case _ =>
        }
    }

    /**
     * Traverse an expression tree and apply f to all elements.
     * Any (sub) expression where f returns true is traversed further.
     * Otherwise traversal stops
     * @param e expression to traverse
     * @param f function to apply to expressions and determining sub expression traversal
     */
    def traverseSameTypeExpTree[T] (e: Exp[T])(implicit f: Exp[T] => Boolean) {
        if (!f (e))
            return

        e match {
            case Def (s) =>
                val subExpr = syms (s)
                subExpr.filter (_.isInstanceOf[Sym[T]]).map (_.asInstanceOf[Sym[T]]).foreach (
                    traverseSameTypeExpTree (_)(f)
                )

            case _ =>
        }
    }


	def findSyms (e: Any)(implicit search: Set[Exp[Any]]): Set[Exp[Any]] = {
        val traversed = e match {
            case s@Sym (_) => Set (s).asInstanceOf[Set[Exp[Any]]]
            case _ => Set.empty[Exp[Any]]
        }
        val startResult = search.filter ((_: Exp[Any]) == e)
        startResult.union (findSymsRec (e, search.asInstanceOf[Set[Exp[Any]]], traversed))
    }

    private def findSymsRec (e: Any, search: Set[Exp[Any]], traversed: Set[Exp[Any]]): Set[Exp[Any]] = {
        val next = (e match {

            case Def (Field (UnboxedTuple (vars), "_1")) => Set (vars (0))
            case Def (Field (UnboxedTuple (vars), "_2")) => Set (vars (1))
            case Def (Field (UnboxedTuple (vars), "_3")) => Set (vars (2))
            case Def (Field (UnboxedTuple (vars), "_4")) => Set (vars (3))
            case Def (Field (UnboxedTuple (vars), "_5")) => Set (vars (4))

            case s@Sym (_) => syms (findDefinition (s)).toSet[Exp[Any]]
            case _ => Set.empty[Exp[Any]]
        }).diff (traversed)
        if (next.isEmpty)
            return Set ()
        val result = search.intersect (next)
        val forward = next.diff (search)
        val nextSeen = traversed.union (forward)

        result.union (
            for (s <- forward;
                 n <- findSymsRec (s, search, nextSeen)
            ) yield
            {
                n
            }
        )
    }

	/*
	private def findSymsRec (e: Any, search: Set[Exp[Any]], traversed: Set[Exp[Any]]): Set[Exp[Any]] = {
        val next = (e match {

            case Def (Tuple2Access1 (UnboxedTuple (vars))) => Set (vars (0))
            case Def (Tuple2Access2 (UnboxedTuple (vars))) => Set (vars (1))

            case Def (Tuple3Access1 (UnboxedTuple (vars))) => Set (vars (0))
            case Def (Tuple3Access2 (UnboxedTuple (vars))) => Set (vars (1))
            case Def (Tuple3Access3 (UnboxedTuple (vars))) => Set (vars (2))

            case Def (Tuple4Access1 (UnboxedTuple (vars))) => Set (vars (0))
            case Def (Tuple4Access2 (UnboxedTuple (vars))) => Set (vars (1))
            case Def (Tuple4Access3 (UnboxedTuple (vars))) => Set (vars (2))
            case Def (Tuple4Access4 (UnboxedTuple (vars))) => Set (vars (3))

            case Def (Tuple5Access1 (UnboxedTuple (vars))) => Set (vars (0))
            case Def (Tuple5Access2 (UnboxedTuple (vars))) => Set (vars (1))
            case Def (Tuple5Access3 (UnboxedTuple (vars))) => Set (vars (2))
            case Def (Tuple5Access4 (UnboxedTuple (vars))) => Set (vars (3))
            case Def (Tuple5Access5 (UnboxedTuple (vars))) => Set (vars (4))

            case s@Sym (_) => syms (findDefinition (s)).toSet[Exp[Any]]
            case _ => Set.empty[Exp[Any]]
        }).diff (traversed)
        if (next.isEmpty)
            return Set ()
        val result = search.intersect (next)
        val forward = next.diff (search)
        val nextSeen = traversed.union (forward)

        result.union (
            for (s <- forward;
                 n <- findSymsRec (s, search, nextSeen)
            ) yield
            {
                n
            }
        )
    }
	 */
}
