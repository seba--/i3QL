package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.functions.TupledFunctionsExpDynamicLambda

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRDistReorderJoins
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRRemoteOperators
	with TupledFunctionsExpDynamicLambda
	with FunctionUtils
{

	override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]],
		equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
	): Rep[Query[(DomainA, DomainB)]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		(relationA, relationB) match {
			case (a, b) if a.remoteDesc > b.remoteDesc =>
				projection(
					equiJoin(b, a, equalities.map(t => (t._2, t._1))),
					fun(
						(bx : Rep[DomainB], ax : Rep[DomainA]) => make_tuple2(ax, bx)
					)(
						manifest[DomainB],
					    manifest[DomainA],
						manifest[(DomainA, DomainB)]
					)
				)
			case (a, Def(eqJoin@EquiJoin(b, c, eqs))) if a.remoteDesc > b.remoteDesc =>
				val mA = mDomA
				val mB = eqJoin.mDomA
				val mC = eqJoin.mDomB

				val aJoinCEqs =
					equalities
						.filter(e => functionHasParameterAccess(e._2, 1) && !functionHasParameterAccess(e._2, 0))
						.map(e => {
							val Def(Lambda(f, x@UnboxedTuple(l), y)) = e._2
							(e._1, dynamicLambda(l(1), y.res))
						})

				val bJoinACEqs1 =
					eqs
						.map(e =>  {
							val Def(Lambda(f1, x1, y1)) = e._1
							val Def(Lambda(f2, x2@UnboxedTuple(l2), y2)) = e._2
							(e._1, dynamicLambda(x1, x2, y2.res))
						})

				val bJoinACEqs2 =
					equalities
						.filter(e => functionHasParameterAccess(e._2, 0) && !functionHasParameterAccess(e._2, 1))
						.map(e => {
							val Def(Lambda(f1, x1, y1)) = e._1
							val Def(Lambda(f2, x2@UnboxedTuple(l2), y2)) = e._2
							(dynamicLambda(l2(0), y2.res), dynamicLambda(x1, l2(1), y1.res))
						})

				projection(
					equiJoin(b, equiJoin(a, c, aJoinCEqs), bJoinACEqs1 ++ bJoinACEqs2),
					fun(
						(b : Rep[_], ac : Rep[(_, _)]) =>
							make_tuple2((tuple2_get1[Any](ac),make_tuple2((b,tuple2_get2[Any](ac)))))
					)(
						mB,
						tupledManifest(mA.asInstanceOf[Manifest[Any]], mC),
						tupledManifest(mB, tupledManifest(mA.asInstanceOf[Manifest[Any]], mC))
					)
				).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]

			case _ =>
				super.equiJoin(relationA, relationB, equalities)

		}
	}



	/**
	 * Checks whether a function accesses its parameters
	 * @param func The function in question
	 * @param accessIndex Parameter index
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



}
