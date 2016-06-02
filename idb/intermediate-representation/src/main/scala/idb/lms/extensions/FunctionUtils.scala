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


import idb.lms.extensions.print.QuoteFunction
import idb.query.colors._

import scala.virtualization.lms.common._
import scala.reflect.SourceContext


/**
 *
 * @author Ralf Mitschke
 */
trait FunctionUtils
    extends TupledFunctionsExp
    with BooleanOpsExp
    with StaticDataExp
    with ExpressionUtils
    //with ScalaOpsPkgExp
{


    def parameterType[A, B] (function: Exp[A => B]): Manifest[Any] = {
        function.tp.typeArguments (0).asInstanceOf[Manifest[Any]]
    }

    def returnType[A, B] (function: Exp[A => B]): Manifest[Any] = {
        function.tp.typeArguments (1).asInstanceOf[Manifest[Any]]
    }

    def parameters[A, B] (function: Exp[A => B]): Seq[Exp[Any]] = {
        parametersAsList (
            parameter (function)
        )
    }

	//override def findDefinition[T](d: Def[T]): Option[Stm] = None

	//TODO Is this right?
    def parametersAsList[A] (params: Exp[A]): Seq[Exp[Any]] = {
        params match {
            case UnboxedTuple (xs) => xs
            case Def(Struct(_,fields)) => fields.map((t : (String, Rep[Any])) => t._2)
            case x => scala.Seq (x)
        }
    }

	/*
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
	*/

    def parameterManifest[A] (a: Exp[A]): Manifest[A] = {
        a.tp.asInstanceOf[Manifest[A]]
    }

    def parameterManifest[A, B] (a: Exp[A], b: Exp[B]): Manifest[Any] = {
        tupledManifest (a.tp, b.tp).asInstanceOf[Manifest[Any]]
        /*
        implicit val ma = a.tp
        implicit val mb = b.tp
        manifest[(A, B)].asInstanceOf[Manifest[Any]]
        */
    }

    def tupledManifest[A, B] (
        implicit ma: Manifest[A], mb: Manifest[B]
    ): Manifest[(A, B)] = {
        manifest[(A, B)]
    }

    def tupledManifest[A, B, C] (
        implicit ma: Manifest[A], mb: Manifest[B], mc: Manifest[C]
    ): Manifest[(A, B, C)] = {
        manifest[(A, B, C)]
    }

    def tupledManifest[A, B, C, D] (
        implicit ma: Manifest[A], mb: Manifest[B], mc: Manifest[C], md: Manifest[D]
    ): Manifest[(A, B, C, D)] = {
        manifest[(A, B, C, D)]
    }

    def tupledManifest[A, B, C, D, E] (
        implicit ma: Manifest[A], mb: Manifest[B], mc: Manifest[C], md: Manifest[D], me: Manifest[E]
    ): Manifest[(A, B, C, D, E)] = {
        manifest[(A, B, C, D, E)]
    }


    def parameter[A, B] (function: Exp[A => B]): Exp[A] = {
        function match {
            case Def (Lambda (_, x: Exp[A], _)) => x
            case c@Const (_) => unboxedFresh[A](c.tp.typeArguments (0).typeArguments (0).asInstanceOf[Manifest[A]])
            case _ => throw new java.lang.IllegalArgumentException ("expected Lambda, found " + function.toString)
        }
    }

    def parameterIndex[A, B] (function: Exp[A => B], x: Exp[Any]): Int = {
        parameters (function).indexOf (x)
    }


    def freeVars[A, B] (function: Exp[A => B]): Seq[Exp[Any]] = {
        val params = parameters (function)
        unusedVars (function, params)
    }

    def unusedVars[A, B] (function: Exp[A => B], vars: Seq[Exp[Any]]): Seq[Exp[Any]] = {
        val varsAsSet = vars.toSet
        val used =
            function match {
                case Def (Lambda (_, _, body)) =>
                    findSyms (body.res)(varsAsSet)
                case Const (_) => scala.collection.Set.empty[Exp[Any]]
                case _ => throw new java.lang.IllegalArgumentException ("expected Lambda, found " + function.toString)
            }
        varsAsSet.diff (used).toList
    }


    def body[A, B] (function: Exp[A => B]): Exp[B] = {
        function match {
            case Def (Lambda (_, _, Block (b))) => b
            case c: Const[B@unchecked] => c // a constant function returns a value of type B
            case _ => throw new java.lang.IllegalArgumentException ("expected Lambda, found " + function.toString)
        }
    }

    def isDisjunctiveParameterEquality[A,B,C](function: Exp[A => Boolean])(implicit mDomX : Manifest[B], mDomY : Manifest[C]): Boolean = {
        val params = parameters (function)
		val b = body (function)

		//If function has one parameter that is a tuple
		//Type of function: Tuple2[B,C] => Boolean
        if (params.size == 1 && isTuple2Manifest(params(0).tp)) {

			val t = params(0).asInstanceOf[Exp[Tuple2[B,C]]]
			val tupledParams = scala.collection.immutable.Set(t._1, t._2)
			b match {
				case Def (Equal (lhs, rhs)) =>
					val usedByLeft = findSyms (lhs)(tupledParams)
					val usedByRight = findSyms (rhs)(tupledParams)
					usedByLeft.size == 1 && usedByRight.size == 1 && usedByLeft != usedByRight

				case _ =>
					false

			}
		//... else if the function has two parameters
		} else if (params.size == 2) {

			b match {
				case Def (Equal (lhs, rhs)) =>
					val usedByLeft = findSyms (lhs)(params.toSet)
					val usedByRight = findSyms (rhs)(params.toSet)
					val bool = usedByLeft.size == 1 && usedByRight.size == 1 && usedByLeft != usedByRight
					bool

				case _ =>
					false

			}
        } else {
			false
		}
    }


    def isTuple2Manifest[T] (m: Manifest[T]): Boolean = m.runtimeClass.getName startsWith "scala.Tuple2"

    def isIdentity[Domain, Range] (function: Rep[Domain => Range]) : Boolean = {
        function match {
			//TODO What about Const?
			case Def(Lambda(_, UnboxedTuple(l1), Block(Def(Struct(tag, fields))))) =>
				l1 == fields.map(t => t._2)
            case Def(Lambda(_, x, Block(body))) =>
                body == x
			//case c@Const(_) =>
            case _ => false
        }
    }

    def returnsLeftOfTuple2[Domain, Range] (function: Rep[Domain => Range]) : Boolean = {
        function match {
           // case Def (Lambda (_, UnboxedTuple (scala.List (a, _)), Block (r))) =>
           //     a == r
			case Def (Lambda (_, t1, Block (body))) =>
				body match {
					case Def(FieldApply(`t1`, "_1")) => true
					case _ => false
				}
            case _ => false
        }
    }

    def returnsRightOfTuple2[Domain, Range] (function: Rep[Domain => Range]) : Boolean = {
        function match {
           // case Def (Lambda (_, UnboxedTuple (scala.List (_, b)), Block (r))) =>
           //     b == r
			case Def (Lambda (_, t1, Block (body))) =>
				body match {
					case Def(FieldApply(`t1`, "_2")) => true
					case _ => false
				}

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

            case Def (Lambda (_, UnboxedTuple (scala.List (a, b)), Block (body)))
				if body == a =>	0
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b)), Block (body)))
				if body == b =>	1

            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c)), Block (body)))
                if body == c => 2


            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d)), Block (body)))
                if body == c => 2
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d)), Block (body)))
                if body == d => 3



            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d, e)), Block (body)))
                if body == a => 0
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d, e)), Block (body)))
                if body == b => 1
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d, e)), Block (body)))
                if body == c => 2
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d, e)), Block (body)))
                if body == d => 3
            case Def (Lambda (_, UnboxedTuple (scala.List (a, b, c, d, e)), Block (body)))
                if body == e => 4

            case Def (Lambda (_, x, Block (body)))
                if body == x => 0

			case Def (Lambda (_, t1, Block (body))) =>
				body match {
					case Def(FieldApply(`t1`, "_1") ) => 0
					case Def(FieldApply(`t1`, "_2") ) => 1
					case Def(FieldApply(`t1`, "_3") ) => 2
					case Def(FieldApply(`t1`, "_4") ) => 3
					case Def(FieldApply(`t1`, "_5") ) => 4
					case _ => -1
				}

            case _ => -1
        }
    }

	/**
	 * Checks whether a function accesses its parameters
	 * @param func The function in question
	 * @param accessIndex Parameter index of the parameter to be checked
	 * @return True, if the function accesses the parameter
	 */
	protected def functionHasParameterAccess(func : Rep[_ => _], accessIndex : Int) : Boolean = {
		func match {
			case Def(Lambda(f, x@UnboxedTuple(l), y)) =>
				var result = false
				val traverseResult = traverseExpTree(y.res)(
				{
					case Def(FieldApply(`x`, s)) if s == s"_${accessIndex + 1}" =>
						result = true //x._1 has been found
						false
					case s@Sym(_) if s == l(accessIndex) =>
						result = true //x._1 has been found
						false
					case _ => true
				}
				)
				result
			case _ =>
				Predef.println(s"RelationalAlgebraIRDistReorderJoins: Warning! $func is not a lambda!")
				false
		}
	}

    /**
     * create a new conjunction.
     * Types are checked dynamically to conform to Domain.
     *
     */
    /*def createConjunction[A : Manifest , B : Manifest , Domain: Manifest] (
        fa: Rep[A => Boolean],
        fb: Rep[B => Boolean]
    ): Rep[Domain => Boolean] = {

        val mDomain = implicitly[Manifest[Domain]]
        val mA : Manifest[A] = fa.tp.typeArguments (0).asInstanceOf[Manifest[A]]
        val mB : Manifest[B] = fb.tp.typeArguments (0).asInstanceOf[Manifest[B]]

        var tupledDomainWithA = false
        var tupledDomainWithB = false

        println(mDomain.runtimeClass.getName)
       // println(Class[Tuple2[Any,Any]])

        if (!(mA >:> mDomain)) {
            if (mDomain.runtimeClass.getName.startsWith("scala.Tuple2") && (mDomain.typeArguments(0) equals mA)) {
                tupledDomainWithA = true
            } else {
                throw new IllegalArgumentException (fa.tp.typeArguments (0) + " must conform to " + mDomain)
            }
        }
        if (!(mB >:> mDomain)) {
            if (mDomain.runtimeClass.getName.startsWith("scala.Tuple2") && (mDomain.typeArguments(1) equals mB)) {
                tupledDomainWithB = true
            } else {
                throw new IllegalArgumentException (fb.tp.typeArguments (0) + " must conform to " + mDomain)
            }
        }

        var faUnsafe : Rep[Domain => Boolean] = null
        var fbUnsafe : Rep[Domain => Boolean] = null

        if (tupledDomainWithA) {
            faUnsafe = fun ((x : Rep[Domain]) => fa(tuple2_get1(x.asInstanceOf[Rep[(A,_)]])))(mDomain, manifest[Boolean])
        } else {
            faUnsafe = fa.asInstanceOf[Rep[Domain => Boolean]]
        }
        if(tupledDomainWithB) {
            fbUnsafe =
                fun ((x : Rep[Domain]) =>
                        fb(tuple2_get2(x.asInstanceOf[Rep[(_,B)]])))(mDomain, manifest[Boolean])
        } else {
            fbUnsafe = fb.asInstanceOf[Rep[Domain => Boolean]]
        }

        val result = fun ((x: Rep[Domain]) => faUnsafe (x) && fbUnsafe (x))(mDomain, manifest[Boolean])
        result
    }  */

    def createConjunction[A, B, Domain: Manifest] (
        fa: Rep[A => Boolean],
        fb: Rep[B => Boolean]
    ): Rep[Domain => Boolean] = {
        val mDomain = implicitly[Manifest[Domain]]

        if (!(fa.tp.typeArguments (0) >:> mDomain)) {
            throw new java.lang.IllegalArgumentException (s"${fa.tp.typeArguments (0)} must conform to $mDomain")
        } else if (!(fb.tp.typeArguments (0) >:> mDomain)) {
            throw new java.lang.IllegalArgumentException (s"${fb.tp.typeArguments (0)} must conform to $mDomain")
        }

        val faUnsafe = fa.asInstanceOf[Rep[Domain => Boolean]]
        val fbUnsafe = fb.asInstanceOf[Rep[Domain => Boolean]]

        val result = fun ((x: Rep[Domain]) => faUnsafe (x) && fbUnsafe (x))(mDomain, manifest[Boolean])
        result
    }

    def stringEffects[T](ef: List[Exp[T]]): String =
        ef.foldRight("")((x,y) => s"${stringExp(x)}, $y")

    def stringExp[T](e: Exp[T]): String = e match {
        case Def(Reify(e, _, ef)) => s"reify(${stringExp(e)}, {${stringEffects(ef)}})"
        case Def(Equal(e1, e2)) => s"${stringExp(e1)} == ${stringExp(e2)}"
        //    case Def(Field(a)) => a
        case Const(c) => c.toString
        case Def(d) => d.toString
        case x => x.toString
    }

    def stringFun[A,B](function: Rep[Function[A,B]]): String = function match {
        case Def (Lambda (_, x, body)) =>
            s"(${x.tp.toString()} => ${stringExp(body.res)}})"
        case Const (c) => "Const" + c.toString
        case _ => throw new IllegalArgumentException ("expected Lambda, found " + function.toString)
    }


    /**
     * create a new conjunction.
     * Types are checked dynamically to conform to Domain.
     *
     */
    def createDisjunction[A, B, Domain: Manifest] (
        fa: Rep[A => Boolean],
        fb: Rep[B => Boolean]
    ): Rep[Domain => Boolean] = {
        val mDomain = implicitly[Manifest[Domain]]
        if (!(fa.tp.typeArguments (0) >:> mDomain)) {
            throw new IllegalArgumentException (s"${fa.tp.typeArguments (0)} must conform to $mDomain")
        } else if (!(fb.tp.typeArguments (0) >:> mDomain)) {
            throw new IllegalArgumentException (s"${fb.tp.typeArguments (0)} must conform to $mDomain")
        }

        val faUnsafe = fa.asInstanceOf[Rep[Domain => Boolean]]
        val fbUnsafe = fb.asInstanceOf[Rep[Domain => Boolean]]

        fun ((x: Rep[Domain]) => faUnsafe (x) || fbUnsafe (x))(mDomain, manifest[Boolean])
    }








}
