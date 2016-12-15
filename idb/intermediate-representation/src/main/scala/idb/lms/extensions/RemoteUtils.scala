package idb.lms.extensions

import idb.algebra.exceptions.NoServerAvailableException
import idb.algebra.ir.RelationalAlgebraIRRemoteOperators
import idb.query.{Host, LocalHost, QueryEnvironment}
import idb.query.taint._

import scala.virtualization.lms.common.{BooleanOpsExp, StaticDataExp, TupledFunctionsExp}



/**
  * @author Mirko Köhler
  */
trait RemoteUtils
	extends TupledFunctionsExp
	with BooleanOpsExp
	with StaticDataExp
	with RelationalAlgebraIRRemoteOperators
{

	def taintOfBase(function : Rep[_ => _], taint : Taint) : Set[Taint] = {
		function match {
			case Def(Lambda(f, x, y)) =>
				taintOfBaseExp(y.res, x, taint)

			case Def(x) =>
				throw new IllegalArgumentException(s"function is not a lambda, instead: $x")

			case _ =>
				throw new IllegalArgumentException(s"function is not a Def")
		}
	}

	protected def taintOfBaseExp(exp : Exp[_], parameter : Exp[_], taint : Taint) : Set[Taint] = {

		if (exp == parameter) {
			return Set(taint)
		}



		exp match {
			case Def(e) =>

				val subExpressions = syms(e)
				//Predef.println(s"definition: $e, sub: $subExpressions")

				if (subExpressions.isEmpty)
					return Set()

				e match {
					case FieldApply(sub, fieldName) =>

						val taintOfSubexpression = taintOfBaseExp(sub, parameter, taint)
						var result = Set.empty[Taint]

						taintOfSubexpression foreach {
							case col@RecordTaint(fieldMap) =>
								val fieldTaint = fieldMap.get(FieldName(fieldName))
								fieldTaint match {
									case Some(c) =>
										result = result ++ Set(c)
									case None =>
										result = result ++ Set(col)
								}
							case col@BaseTaint(_) =>
								result = result ++ Set(col)
						}

						return result

					case _ =>
						val taintsOfSubexpressions = subExpressions.map(x => taintOfBaseExp(x, parameter, taint))
						return taintsOfSubexpressions.fold(Set())((a, b) => a ++ b)
				}

			case Const(_) =>
				//Constants do not have a taint
				Set()

			case Sym(_) =>
				//Sym has no Def -> look if parameter is a tuple and the exp is a sym in the tuple
				parameter match {
					case UnboxedTuple(l) =>
						var i = 0
						l.foreach(e =>  {
							i = i + 1
							if (exp == e) {
								taint match {
									case RecordTaint(fieldMap) =>
										val fieldTaint = fieldMap.get(FieldName(s"_$i"))
										fieldTaint match {
											case Some(c) => return Set(c)
											case None => return Set(taint)
										}

									case _ => return Set(taint)
								}
							}
						})
					case _ =>
				}
				//Sym was not found -> taint it in every way possible to avoid security risks
				System.err.println(s"Sym was not found, taint with all colors possible: $exp, parameter = $parameter")
				Set(taint)

			case _ =>
				throw new IllegalArgumentException(s"No def: $exp")
		}
	}


	def taintOfProjection(taint : Taint, func: Rep[_ => _]): Taint = func match {

		case Def(Lambda(f, x, y)) =>
			y.res match {
				case Def(Struct(tags, fields)) =>
					var taintMap : Map[FieldId, Taint] = Predef.Map.empty

					fields.foreach(t => {
						val fieldName = t._1
						val exp = t._2
						//TODO: Add recursive calls here!
						taintMap = taintMap + (FieldName(fieldName) -> Taint.toBaseTaint(taintOfBaseExp(exp, x, taint)))
					})
					//Predef.println(s"Struct $tags with $fields")
					RecordTaint(taintMap)
				case yRes =>
					Taint.toBaseTaint(taintOfBaseExp(yRes, x, taint))
			}

		/*case Def(Lambda(f, x, y)) =>
			Predef.println("No UnboxedTuple")
			Color.toBaseTaint(colorsOfTFields(func, taint))  */

		case _ =>
			Predef.println(s"Warning! $func is not a lambda!")
			Taint.empty
	}
}
