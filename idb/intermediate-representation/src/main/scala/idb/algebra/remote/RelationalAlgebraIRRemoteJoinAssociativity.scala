package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.functions.TupledFunctionsExpDynamicLambda
import idb.query.QueryEnvironment

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRRemoteJoinAssociativity
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

				case (a, Def(eqJoin@EquiJoin(b, c, eqs)))
					if a.color == b.color && b.color != c.color
						&& equalities.forall((t) => !(functionHasParameterAccess(t._2,0) && functionHasParameterAccess(t._2,1)) ) //Checks whether the equality functions can be reordered.

				=>
					//a >< (b >< c) --> (a >< b) >< c
					val mA = mDomA
					val mB = eqJoin.mDomA
					val mC = eqJoin.mDomB

					//a => a.f, (b, c) => b.f  -->  a => a.f, b => b.f
					val aJoinBEqs =
						equalities
							.filter(e => !functionHasParameterAccess(e._2, 1))
							.map(e => {
								val Def(Lambda(f, x@UnboxedTuple(l), y)) = e._2
								(e._1, dynamicLambda(l(0), y.res))
							})

					//a => a.f, (b,c) => c.f  -->  (a,b) => a.f, c => c.f
					val aJoinCEqs =
						equalities
							.filter(e => !functionHasParameterAccess(e._2, 0))
							.map(e =>  {
								val Def(Lambda(f1, x1, y1)) = e._1
								val Def(Lambda(f2, x2@UnboxedTuple(l2), y2)) = e._2
								(dynamicLambda(x1, l2(0), y1.res), dynamicLambda(l2(1), y2.res))
							})

					//b => b.f, c => c.f  -->  (a,b) => b.f, c => c.f
					val bJoinCEqs =
						eqs
							.map(e => {
								val Def(Lambda(f1, x1, y1)) = e._1
								val Def(Lambda(f2, x2, y2)) = e._2
								(dynamicLambda(x2, x1, y1.res), e._2) //<-- x2 is used as fresh variable here!
							})

					projection(
						equiJoin(equiJoin(a, b, aJoinBEqs), c, aJoinCEqs ++ bJoinCEqs),
						fun(
							(ab : Rep[(_,_)], c : Rep[_]) =>
								make_tuple2((tuple2_get1[Any](ab),make_tuple2((tuple2_get2[Any](ab), c))))
						)(
								tupledManifest(mA.asInstanceOf[Manifest[Any]], mB),
								mC,
								tupledManifest(mA.asInstanceOf[Manifest[Any]], tupledManifest(mB.asInstanceOf[Manifest[Any]], mC))
						)
					).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]


				case (Def(eqJoin@EquiJoin(a, b, eqs)), c)
					if b.color == c.color && a.color != b.color
					&& equalities.forall((t) => !(functionHasParameterAccess(t._1,0) && functionHasParameterAccess(t._1,1)) ) //Checks whether the equality functions can be reordered.
					 =>
					//(a >< b) >< c --> a >< (b >< c)
					val mA = eqJoin.mDomA
					val mB = eqJoin.mDomB
					val mC = mDomB

					//(a, b) => a.f, c => c.f  -->  a => a.f, (b, c) => c.f
					val aJoinCEqs =
						equalities
							.filter(e => !functionHasParameterAccess(e._1, 1))
							.map(e => {
								val Def(Lambda(f1, x1@UnboxedTuple(l1), y1)) = e._1
								val Def(Lambda(f2, x2, y2)) = e._2
								(dynamicLambda(l1(0), y1.res), dynamicLambda(l1(1), x2, y2.res))
							})

					//(a, b) => b.f, c => c.f  -->  b => b.f, c => c.f
					val bJoinCEqs =
						equalities
							.filter(e => !functionHasParameterAccess(e._1, 0))
							.map(e => {
							val Def(Lambda(f1, x1@UnboxedTuple(l1), y1)) = e._1
							val Def(Lambda(f2, x2, y2)) = e._2
							(dynamicLambda(l1(1), y1.res), e._2)
						})


					//a => a.f, b => b.f  -->  a => a.f, (b, c) => b.f
					val aJoinBEqs =
						eqs //<-- based on eqs!
							.map(e =>  {
								val Def(Lambda(f1, x1, y1)) = e._1
								val Def(Lambda(f2, x2, y2)) = e._2
								(e._1, dynamicLambda(x2, x1, y2.res))// <-- x1 is used as fresh var
							})



					projection(
						equiJoin(a, equiJoin(b, c, bJoinCEqs), aJoinCEqs ++ aJoinBEqs),
						fun(
							(a : Rep[_], bc : Rep[(_, _)]) =>
								make_tuple2(
									(make_tuple2((a, tuple2_get1[Any](bc))),
										tuple2_get2[Any](bc))
								)
						)(
								mA,
								tupledManifest(mB, mC.asInstanceOf[Manifest[Any]]),
								tupledManifest(tupledManifest(mA, mB), mC.asInstanceOf[Manifest[Any]])
							)
					).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]

				case _ =>
					super.equiJoin(relationA, relationB, equalities)

			}
		}
	}
