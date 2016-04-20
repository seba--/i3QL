package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.functions.TupledFunctionsExpDynamicLambda
import idb.query.colors.Color
import idb.query.{QueryEnvironment}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRRemoteReorderJoins
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRRemoteOperators
	with TupledFunctionsExpDynamicLambda
	with FunctionUtils
{

	override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]],
		equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		(relationA, relationB) match {

			case (a, b) if isGreater(a.color, b.color, queryEnvironment) =>
				//a >< b --> b >< a
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
			case (a, Def(eqJoin@EquiJoin(b, c, eqs)))
				if isGreater(a.color, b.color, queryEnvironment)
					&& equalities.forall((t) => !(functionHasParameterAccess(t._2,0) && functionHasParameterAccess(t._2,1)) ) //Checks whether the equality functions can be reorder, ie there is no (b,c) => b.f + c.f, for example.
			=>

				//a >< (b >< c) --> b >< (a >< c)
				val mA = mDomA
				val mB = eqJoin.mDomA
				val mC = eqJoin.mDomB

				//a => a.f, (b, c) => c.f  -->  a => a.f, c => c.f
				val aJoinCEqs =
					equalities
						.filter(e => !functionHasParameterAccess(e._2, 0))
						.map(e => {
							val Def(Lambda(f, x@UnboxedTuple(l), y)) = e._2
							(e._1, dynamicLambda(l(1), y.res))
						})

				//b => b.f, c => c.f  -->  b => b.f, (a, c) => c.f
				val bJoinACEqs1 =
					eqs //<-- based on eqs!
						.map(e =>  {
							val Def(Lambda(f1, x1, y1)) = e._1
							val Def(Lambda(f2, x2, y2)) = e._2
							(e._1, dynamicLambda(x1, x2, y2.res)) //x1 is just used as unknown var
						})

				//a => a.f, (b, c) => b.f  -->  b => b.f, (a, c) => a.f
				val bJoinACEqs2 =
					equalities
						.filter(e => !functionHasParameterAccess(e._2, 1))
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


			case (Def(eqJoin@EquiJoin(a, b, eqs)), c)
				if isGreater(b.color, c.color, queryEnvironment)
					&& equalities.forall((t) => !(functionHasParameterAccess(t._1,0) && functionHasParameterAccess(t._1,1)) ) //Checks whether the equality functions can be reorder, ie there is no (a,b) => a.f + b.f, for example.
			=>
				//(a >< b) >< c --> (a >< c) >< b
				val mA = eqJoin.mDomA
				val mB = eqJoin.mDomB
				val mC = mDomB

				//(a, b) => a.f, c => c.f  -->  a => a.f, c => c.f
				val aJoinCEqs =
					equalities
						.filter(e => !functionHasParameterAccess(e._1, 1))
						.map(e => {
							val Def(Lambda(f, x@UnboxedTuple(l), y)) = e._1
							(dynamicLambda(l(0), y.res), e._2)
					})

				//a => a.f, b => b.f  -->  (a, c) => a.f, b => b.f
				val aBJoinCEqs1 =
					eqs //<-- based on eqs!
						.map(e =>  {
						val Def(Lambda(f1, x1, y1)) = e._1
						val Def(Lambda(f2, x2, y2)) = e._2
						(dynamicLambda(x1, x2, y1.res), e._2)
					})

				//(a, b) => b.f, c => c.f  --> (a, c) => c.f, b => b.f
				val aBJoinCEqs2 =
					equalities
						.filter(e => !functionHasParameterAccess(e._1, 0))
						.map(e => {
						val Def(Lambda(f1, x1@UnboxedTuple(l1), y1)) = e._1
						val Def(Lambda(f2, x2, y2)) = e._2
						(dynamicLambda(l1(0), x2, y2.res), dynamicLambda(l1(1), y1.res))
					})

				projection(
					equiJoin(equiJoin(a, c, aJoinCEqs), b, aBJoinCEqs1 ++ aBJoinCEqs2),
					fun(
						(ac : Rep[(_,_)], b : Rep[(_)]) =>
							make_tuple2(
								(make_tuple2((tuple2_get1[Any](ac), b)),
								tuple2_get2[Any](ac)))
					)(
							tupledManifest(mA, mC.asInstanceOf[Manifest[Any]]),
							mB,
							tupledManifest(tupledManifest(mA, mB), mC.asInstanceOf[Manifest[Any]])
						)
				).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]



			case _ =>
				super.equiJoin(relationA, relationB, equalities)

		}
	}

	private def isGreater(a : Color, b : Color, env : QueryEnvironment) : Boolean =
		isSmallerDef(b, a, env)

	private def isSmaller(a : Color, b : Color, env : QueryEnvironment) : Boolean =
		isSmallerDef(a, b, env)

	val isSmallerDef : (Color, Color, QueryEnvironment) => Boolean =
		isSmallerImpl1

	private def isSmallerImpl1(a : Color, b : Color, env : QueryEnvironment) : Boolean =
		false


	/*private def isSmallerImpl2(a : Color, b : Color, env : QueryEnvironment) : Boolean = {
		val aPermissions = (env permission a) sortWith ((h1, h2) => h1.name < h2.name)
		val bPermissions = (env permission b) sortWith ((h1, h2) => h1.name < h2.name)

		if (aPermissions equals bPermissions) {
			false
		} else if (aPermissions.isEmpty && bPermissions.nonEmpty) {
			false
		} else if (bPermissions.isEmpty) {
			true
		} else {
			false
		}
	}    */


}
