package idb.lms.extensions

import idb.algebra.exceptions.NoServerAvailableException
import idb.algebra.ir.RelationalAlgebraIRRemoteOperators
import idb.query.{Host, LocalHost, QueryEnvironment}
import idb.query.colors._

import scala.virtualization.lms.common.{BooleanOpsExp, StaticDataExp, TupledFunctionsExp}



/**
  * @author Mirko Köhler
  */
trait ColorUtils
	extends TupledFunctionsExp
	with BooleanOpsExp
	with StaticDataExp
	with RelationalAlgebraIRRemoteOperators
{

	def colorsOfTFields(func : Rep[_ => _], coloring : Color) : Set[Color] = {
		func match {
			case Def(Lambda(f, x, y)) =>
				colorsOfTFieldsInExp(y.res, x, coloring)

			case _ =>
				Predef.println(s"Warning! $func is not a lambda!")
				Set.empty
		}
	}

	protected def colorsOfTFieldsInExp(exp : Exp[_], parameter : Exp[_], coloring : Color) : Set[Color] = {
		//TODO: Add special treatment if parameter is tuple!
		if (exp == parameter) {
			//Predef.println(s"exp == parameter --> $coloring")
			return Set(coloring)
		}

		exp match {
			case Def(e) =>

				val subExpressions = syms(e)
				//Predef.println(s"definition: $e, sub: $subExpressions")

				if (subExpressions.isEmpty)
					return Set()

				e match {
					case FieldApply(sub, fieldName) =>
						val colorOfSubexpression = colorsOfTFieldsInExp(sub, parameter, coloring)
						var result = Set.empty[Color]

						colorOfSubexpression.foreach({
							case col@FieldColor(fieldMap) =>
								val fieldColor = fieldMap.get(FieldName(fieldName))
								fieldColor match {
									case Some(c) =>
										result = result ++ Set(c)
									case None =>
										result = result ++ Set(col)
								}
							case col@ClassColor(_) =>
								result = result ++ Set(col)
						})
						return result

					case _ =>
						val colorsOfSubexpressions = subExpressions.map(x => colorsOfTFieldsInExp(x, parameter, coloring))
						return colorsOfSubexpressions.fold(Set())((a, b) => a ++ b)
				}

			case Const(_) =>
				//Constants do not have a color
				Set()

			case Sym(_) =>
				//Sym was not found -> color it in every way possible to avoid security risks
				System.err.println("Sym was not found, color with all colors possible: " + exp)
				Set(coloring)

			case _ =>
				throw new IllegalArgumentException(s"No def: $exp")
		}
	}


	def projectionColor(color : Color, func: Rep[_ => _]): Color = func match {

		case Def(Lambda(f, x, y)) =>
			y.res match {
				case Def(Struct(tags, fields)) =>
					var colorMap : Map[FieldId, Color] = Predef.Map.empty

					fields.foreach(t => {
						val fieldName = t._1
						val exp = t._2
						//TODO: Add recursive calls here!
						colorMap = colorMap + (FieldName(fieldName) -> Color.fromIdsInColors(colorsOfTFieldsInExp(exp, x, color)))
					})
					//Predef.println(s"Struct $tags with $fields")
					FieldColor(colorMap)
				case yRes =>
					Color.fromIdsInColors(colorsOfTFieldsInExp(yRes, x, color))
			}

		/*case Def(Lambda(f, x, y)) =>
			Predef.println("No UnboxedTuple")
			Color.fromIdsInColors(colorsOfTFields(func, color))  */

		case _ =>
			Predef.println(s"Warning! $func is not a lambda!")
			Color.empty
	}

//	def findPossibleHosts(colorIds : Set[ColorId], env : QueryEnvironment) : Set[Host] = {
//		env.hosts.foldLeft(Set.empty[Host])((set, h) =>
//			if (colorIds subsetOf env.permissionsOf(h))
//				set + h
//			else
//				set
//		)
//	}
//
//
//	def findBestHostInCollection(hosts : Iterable[Host], queryEnvironment: QueryEnvironment) : Host = {
//
//		var bestHost : Option[Host] = None
//		var permissionCount : Int = 0
//		var priority : Int = Int.MinValue
//		hosts.foreach(h => {
//			val c = queryEnvironment.permissionsOf(h).size
//			if (bestHost == null || priority < queryEnvironment.priorityOf(h) || permissionCount <= c) {
//				bestHost = Some(h)
//				permissionCount = c
//			}
//		})
//
//		bestHost.getOrElse(throw new NoServerAvailableException())
//	}


	def distributeRelations[DomainA : Manifest, DomainB : Manifest, Range : Manifest](
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]],
		constructor : (Rep[Query[DomainA]], Rep[Query[DomainB]]) => Rep[Query[Range]]
	)(implicit queryEnvironment: QueryEnvironment) : Rep[Query[Range]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		val hostA : Host = relationA.host
		val hostB : Host = relationB.host
		val colA : Color = relationA.color
		val colB : Color = relationB.color


		//The hosts are the same -> No need for distribution
		if (hostA == hostB)
			return constructor(relationA, relationB)

		val allColors = colA.ids union colB.ids
		val oldHost = idb.query.findHost(queryEnvironment, scala.collection.Seq(hostA, hostB), allColors)

		oldHost match {
			case Some(a) if a == hostA =>
				return constructor(relationA, remote(relationB, hostA))
			case Some(b) if b == hostB =>
				return constructor(remote(relationA, hostB), relationB)
			case None =>
				val newHost = idb.query.findHost(queryEnvironment, allColors)
				newHost match {
					case Some(h) =>
						return constructor(remote(relationA, h), remote(relationB, h))
					case None =>
						throw new NoServerAvailableException(allColors)
				}

		}

		throw new UnsupportedOperationException("What's that?")
	}

}
