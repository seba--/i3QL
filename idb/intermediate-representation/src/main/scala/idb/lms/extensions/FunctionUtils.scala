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

import scala.virtualization.lms.common.TupledFunctionsExp

/**
 *
 * @author Ralf Mitschke
 */
trait FunctionUtils
    extends TupledFunctionsExp
    with ExpressionUtils
{

    def parameterType[A, B] (function: Exp[A => B]): Manifest[Any] = {
        function.tp.typeArguments (0).asInstanceOf[Manifest[Any]]
    }

    def returnType[A, B] (function: Exp[A => B]): Manifest[Any] = {
        function.tp.typeArguments (1).asInstanceOf[Manifest[Any]]
    }

    def parameters[A, B] (function: Exp[A => B]): List[Exp[Any]] = {
        parametersAsList (
            parameter (function)
        )
    }

    def parametersAsList[A] (params: Exp[A]): List[Exp[Any]] = {
        params match {
            case UnboxedTuple (xs) => xs
            case Def (ETuple2 (a, b)) => List (a, b)
            case Def (ETuple3 (a, b, c)) => List (a, b, c)
            case Def (ETuple4 (a, b, c, d)) => List (a, b, c, d)
            case Def (ETuple5 (a, b, c, d, e)) => List (a, b, c, d, e)
            case x => List (x)
        }
    }

    def parameterManifest[A] (a: Exp[A]): Manifest[Any] = {
        a.tp.asInstanceOf[Manifest[Any]]
    }

    def parameterManifest[A, B] (a: Exp[A], b: Exp[B]): Manifest[Any] = {
        implicit val ma = a.tp
        implicit val mb = b.tp
        manifest[(A, B)].asInstanceOf[Manifest[Any]]
    }

    def parameter[A, B] (function: Exp[A => B]): Exp[A] = {
        function match {
            case Def (Lambda (_, x: Exp[A], _)) => x
            case c@Const (_) => unboxedFresh[A](c.tp.typeArguments (0).typeArguments (0).asInstanceOf[Manifest[A]])
            case _ => throw new IllegalArgumentException ("expected Lambda, found " + function)
        }
    }

    def parameterIndex[A, B] (function: Exp[A => B], x: Exp[Any]): Int = {
        parameters (function).indexOf (x)
    }


    def freeVars[A, B] (function: Exp[A => B]): List[Exp[Any]] = {
        val params = parameters (function).toSet
        val used =
            function match {
                case Def (Lambda (_, _, body)) =>
                    findSyms (body.res)(params).asInstanceOf[Set[Exp[Any]]]
                case Const (_) => Set.empty[Exp[Any]]
                case _ => throw new IllegalArgumentException ("expected Lambda, found " + function)
            }
        params.diff (used).toList
    }


    def body[A, B] (function: Exp[A => B]): Exp[B] = {
        function match {
            case Def (Lambda (_, _, Block (b))) => b
            case c: Const[B@unchecked] => c // a constant function returns a value of type B
            case _ => throw new IllegalArgumentException ("expected Lambda, found " + function)
        }
    }

    def isDisjunctiveParameterEquality[A] (function: Exp[A => Boolean]): Boolean = {
        val params = parameters (function)
        if (params.size != 2) {
            return false
        }
        body (function) match {
            case Def (Equal (lhs, rhs)) => {
                val usedByLeft = findSyms (lhs)(params.toSet)
                val usedByRight = findSyms (rhs)(params.toSet)
                usedByLeft.size == 1 && usedByRight.size == 1 && usedByLeft != usedByRight
            }
            case _ => false
        }
    }


    def isIdentity[Domain, Range] (function: Rep[Domain => Range]) = {
        function match {
            case Def (Lambda (_, UnboxedTuple (List (a1, b1)), Block (Def (ETuple2 (a2, b2))))) =>
                a1 == a2 && b1 == b2
            case Def (Lambda (_, UnboxedTuple (List (a1, b1, c1)), Block (Def (ETuple3 (a2, b2, c2))))) =>
                a1 == a2 && b1 == b2 && c1 == c2
            case Def (Lambda (_, UnboxedTuple (List (a1, b1, c1, d1)), Block (Def (ETuple4 (a2, b2, c2, d2))))) =>
                a1 == a2 && b1 == b2 && c1 == c2 && d1 == d2
            case Def (Lambda (_, UnboxedTuple (List (a1, b1, c1, d1, e1)), Block (Def (ETuple5 (a2, b2, c2, d2, e2))))) =>
                a1 == a2 && b1 == b2 && c1 == c2 && d1 == d2 && e1 == e2
            case Def (Lambda (_, x, Block (body))) =>
                body == x
            case _ => false
        }
    }

    def isObjectEquality[DomainA, DomainB] (
        equality: (Rep[DomainA => Any], Rep[DomainB => Any])
    ): Boolean = equality match {
        case (Def (Lambda (_, a, Block (bodyA))), Def (Lambda (_, b, Block (bodyB)))) =>
            bodyA.equals (a) && bodyB.equals (b) // TODO why was == not accepted by compiler?
        case _ => false
    }

    /**
     *
     * @return The index of the parameter returned by the function if it has a form similar to (x,y) => x
     *         If the body does not simply return a parameter -1 is returned by this function.
     */
    def returnedParameter[Domain, Range] (function: Rep[Domain => Range]): Int = {
        implicit val mRange = returnType (function).asInstanceOf[Manifest[Range]]
        function match {

            case Def (Lambda (_, UnboxedTuple (List (a, b)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (List (a, b)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple2Access1 (t)))))
                if p == t => 0
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple2Access2 (t)))))
                if p == t => 1


            case Def (Lambda (_, UnboxedTuple (List (a, b, c)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (List (a, b, c)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, UnboxedTuple (List (a, b, c)), Block (body)))
                if body == c => 2
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple3Access1 (t)))))
                if p == t => 0
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple3Access2 (t)))))
                if p == t => 1
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple3Access3 (t)))))
                if p == t => 2


            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d)), Block (body)))
                if body == c => 2
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d)), Block (body)))
                if body == d => 3
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple4Access1 (t)))))
                if p == t => 0
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple4Access2 (t)))))
                if p == t => 1
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple4Access3 (t)))))
                if p == t => 2
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple4Access4 (t)))))
                if p == t => 3


            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d, e)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d, e)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d, e)), Block (body)))
                if body == c => 2
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d, e)), Block (body)))
                if body == d => 3
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d, e)), Block (body)))
                if body == e => 4
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple5Access1 (t)))))
                if p == t => 0
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple5Access2 (t)))))
                if p == t => 1
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple5Access3 (t)))))
                if p == t => 2
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple5Access4 (t)))))
                if p == t => 3
            case Def (Lambda (_, p: UnboxedTuple[_], Block (Def (Tuple5Access5 (t)))))
                if p == t => 4

            case Def (Lambda (_, x, Block (body)))
                if body == x => 0

            case _ => -1
        }
    }

}
