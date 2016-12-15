package idb.algebra.remote.opt

import idb.algebra.{QueryTransformer, QueryTransformerAdapter}
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.functions.TupledFunctionsExpDynamicLambda
import idb.lms.extensions.{FunctionUtils, RemoteUtils}
import idb.query.QueryEnvironment
import idb.query.taint.Taint

trait StandardRemoteOptimization extends QueryTransformerAdapter with QueryTaint {
	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with TupledFunctionsExpDynamicLambda
		with RemoteUtils
		with FunctionUtils
	import IR._

	override def transform[Domain: Manifest](relation: Rep[Query[Domain]])(implicit env: QueryEnvironment): Rep[Query[Domain]] = relation match {
		//Push down selections and projections
		case Def(Selection( Def(Remote(q, host)), f)) =>
			super.transform(remote(selection(transform(q), f), host))
		case Def(Projection( Def(Remote(q, host)), f)) =>
			super.transform(remote(projection(transform(q), f), host))

		case Def(op@CrossProduct(qa, qb)) =>
			val transA = transform(qa)
			val transB = transform(qb)

			val res = Commutativity.ruleCrossProduct(transA, transB)(op.mDomA, op.mDomB, env).asInstanceOf[Rep[Query[Domain]]]

			if (res == relation)
				super.transform(Associativity.ruleCrossProduct(transA, transB)(op.mDomA, op.mDomB, env).asInstanceOf[Rep[Query[Domain]]])
			else
				super.transform(res)

		case Def(op@EquiJoin(qa, qb, eqs)) =>
			val transA = transform(qa)
			val transB = transform(qb)

			val res = Commutativity.ruleEquiJoin(transA, transB, eqs)(op.mDomA, op.mDomB, env).asInstanceOf[Rep[Query[Domain]]]

			if (res == relation)
				super.transform(Associativity.ruleEquiJoin(transA, transB, eqs)(op.mDomA, op.mDomB, env).asInstanceOf[Rep[Query[Domain]]])
			else
				super.transform(res)

		case _ =>
			super.transform(pushTransform(relation))
	}

	/**
	  * Definition of commutativity rules for each operator.
	  */
	object Commutativity {
		def ruleCrossProduct[DomainA : Manifest, DomainB : Manifest](
			relationA : Rep[Query[DomainA]],
			relationB : Rep[Query[DomainB]]
		)(implicit env : QueryEnvironment) : Rep[Query[(DomainA, DomainB)]] = {
			val mDomA = implicitly[Manifest[DomainA]]
			val mDomB = implicitly[Manifest[DomainB]]
			(relationA, relationB) match {
				case (a, b) if isGreater(taintOf(a), taintOf(b), env) =>
					//a x b --> b x a
					transform(
						IR.projection (
							IR.crossProduct(b, a),
							fun(
								(bx : Rep[DomainB], ax : Rep[DomainA]) => make_tuple2(ax, bx)
							)(
								mDomB,
								mDomA,
								manifest[(DomainA, DomainB)]
							)
						)
					)

				case (a, Def(cross@CrossProduct(b, c))) if isGreater(taintOf(a), taintOf(b), env) =>
					//a x (b x c) --> b x (a x c)
					val mA = mDomA
					val mB = cross.mDomA
					val mC = cross.mDomB

					transform(
						IR.projection(
							IR.crossProduct(b, IR.crossProduct(a, c)),
							fun(
								(b : Rep[_], ac : Rep[(_, _)]) =>
									make_tuple2((tuple2_get1[Any](ac),make_tuple2((b,tuple2_get2[Any](ac)))))
							)(
								mB,
								tupledManifest(mA.asInstanceOf[Manifest[Any]], mC),
								tupledManifest(mB, tupledManifest(mA.asInstanceOf[Manifest[Any]], mC))
							)
						).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]
					)

				case (Def(cross@CrossProduct(a, b)), c)	if isGreater(taintOf(b), taintOf(c), env) =>
					//(a x b) x c --> (a x c) x b
					val mA = cross.mDomA
					val mB = cross.mDomB
					val mC = mDomB

					transform(
						projection(
							crossProduct(crossProduct(a, c), b),
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
					)

				case _ => IR.crossProduct(relationA, relationB)

			}
		}

		def ruleEquiJoin[DomainA: Manifest, DomainB: Manifest] (
			relationA: Rep[Query[DomainA]],
			relationB: Rep[Query[DomainB]],
			equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
		)(implicit env : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
			val mDomA = implicitly[Manifest[DomainA]]
			val mDomB =  implicitly[Manifest[DomainB]]

			(relationA, relationB) match {

				case (a, b) if isGreater(taintOf(a), taintOf(b), env) =>
					//a >< b --> b >< a
					transform(
						IR.projection(
							IR.equiJoin(b, a, equalities.map(t => (t._2, t._1))),
							fun(
								(bx : Rep[DomainB], ax : Rep[DomainA]) => make_tuple2(ax, bx)
							)(
								manifest[DomainB],
								manifest[DomainA],
								manifest[(DomainA, DomainB)]
							)
						)
					)
				case (a, Def(eqJoin@EquiJoin(b, c, eqs)))
					if isGreater(taintOf(a), taintOf(b), env)
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

					transform (
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
					)


				case (Def(eqJoin@EquiJoin(a, b, eqs)), c)
					if isGreater(taintOf(b), taintOf(c), env)
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

					transform (
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
					)


				case _ =>
					equiJoin(relationA, relationB, equalities)

			}
		}
	}

	/**
	  * Associativity rules for each operator.
	  */
	object Associativity {
		def ruleCrossProduct[DomainA: Manifest, DomainB: Manifest] (
			relationA: Rep[Query[DomainA]],
			relationB: Rep[Query[DomainB]]
		)(implicit env : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
			val mDomA = implicitly[Manifest[DomainA]]
			val mDomB = implicitly[Manifest[DomainB]]

			(relationA, relationB) match {
				case (a, Def(cross@CrossProduct(b, c)))	if taintOf(a) == taintOf(b) && taintOf(b) != taintOf(c) =>
					//a x (b x c) --> (a x b) x c

					val mA = mDomA
					val mB = cross.mDomA
					val mC = cross.mDomB

					transform (
						projection(
							crossProduct(crossProduct(a, b), c),
							fun(
								(ab: Rep[(_, _)], c: Rep[_]) =>
									make_tuple2((tuple2_get1[Any](ab), make_tuple2((tuple2_get2[Any](ab), c))))
							)(
								tupledManifest(mA.asInstanceOf[Manifest[Any]], mB),
								mC,
								tupledManifest(mA.asInstanceOf[Manifest[Any]], tupledManifest(mB.asInstanceOf[Manifest[Any]], mC))
							)
						).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]
					)


				case (Def(cross@CrossProduct(a, b)), c) if taintOf(b) == taintOf(c) && taintOf(a) != taintOf(b) =>
					//(a x b) x c --> a x (b x c)
					val mA = cross.mDomA
					val mB = cross.mDomB
					val mC = mDomB

					transform (
						projection(
							crossProduct(a, crossProduct(b, c)),
							fun(
								(a: Rep[_], bc: Rep[(_, _)]) =>
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
					)


				case _ => crossProduct(relationA, relationB)

			}
		}

		def ruleEquiJoin[DomainA: Manifest, DomainB: Manifest] (
			relationA: Rep[Query[DomainA]],
			relationB: Rep[Query[DomainB]],
			equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
		)(implicit env : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {

			val mDomA = implicitly[Manifest[DomainA]]
			val mDomB = implicitly[Manifest[DomainB]]

			(relationA, relationB) match {

				case (a, Def(eqJoin@EquiJoin(b, c, eqs)))
					if taintOf(a) == taintOf(b) && taintOf(b) != taintOf(c)
						&& equalities.forall((t) => !(functionHasParameterAccess(t._2, 0) && functionHasParameterAccess(t._2, 1))) //Checks whether the equality functions can be reordered.
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
						.map(e => {
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

					transform (
						projection(
							equiJoin(equiJoin(a, b, aJoinBEqs), c, aJoinCEqs ++ bJoinCEqs),
							fun(
								(ab: Rep[(_, _)], c: Rep[_]) =>
									make_tuple2((tuple2_get1[Any](ab), make_tuple2((tuple2_get2[Any](ab), c))))
							)(
								tupledManifest(mA.asInstanceOf[Manifest[Any]], mB),
								mC,
								tupledManifest(mA.asInstanceOf[Manifest[Any]], tupledManifest(mB.asInstanceOf[Manifest[Any]], mC))
							)
						).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]
					)


				case (Def(eqJoin@EquiJoin(a, b, eqs)), c)
					if taintOf(b) == taintOf(c) && taintOf(a) != taintOf(b)
						&& equalities.forall((t) => !(functionHasParameterAccess(t._1, 0) && functionHasParameterAccess(t._1, 1))) //Checks whether the equality functions can be reordered.
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
						.map(e => {
						val Def(Lambda(f1, x1, y1)) = e._1
						val Def(Lambda(f2, x2, y2)) = e._2
						(e._1, dynamicLambda(x2, x1, y2.res))// <-- x1 is used as fresh var
					})

					transform (
						projection(
							equiJoin(a, equiJoin(b, c, bJoinCEqs), aJoinCEqs ++ aJoinBEqs),
							fun(
								(a: Rep[_], bc: Rep[(_, _)]) =>
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
					)


				case _ =>
					equiJoin(relationA, relationB, equalities)
			}
		}
	}


	private def isGreater(a : Taint, b : Taint, env : QueryEnvironment) : Boolean =
		isSmallerDef(b, a, env)

	private def isSmaller(a : Taint, b : Taint, env : QueryEnvironment) : Boolean =
		isSmallerDef(a, b, env)

	private val isSmallerDef : (Taint, Taint, QueryEnvironment) => Boolean =
		isSmallerImpl1

	private def isSmallerImpl1(a : Taint, b : Taint, env : QueryEnvironment) : Boolean =
		false


}
