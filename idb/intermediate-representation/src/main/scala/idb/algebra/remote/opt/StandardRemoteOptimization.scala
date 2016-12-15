package idb.algebra.remote.opt

import idb.algebra.{QueryTransformer, QueryTransformerAdapter}
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.{FunctionUtils, RemoteUtils}
import idb.query.QueryEnvironment
import idb.query.taint.Taint

trait StandardRemoteOptimization extends QueryTransformerAdapter with QueryTaint {
	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RemoteUtils
		with FunctionUtils
	import IR._

	override def transform[Domain: Manifest](relation: Rep[Query[Domain]])(implicit env: QueryEnvironment): Rep[Query[Domain]] = relation match {
		//Push down selections and projections
		case Def(Selection( Def(Remote(q, host)), f)) =>
			remote(selection(transform(q), f), host)
		case Def(Projection( Def(Remote(q, host)), f)) =>
			remote(projection(transform(q), f), host)

		case Def(op@CrossProduct(qa, qb)) =>
			Commutativity.crossProduct(qa, qb)(op.mDomA, op.mDomB, env).asInstanceOf[Rep[Query[Domain]]]



	}

	object Commutativity {
		def crossProduct[DomainA : Manifest, DomainB : Manifest](qa : Rep[Query[DomainA]], qb : Rep[Query[DomainB]])(implicit env : QueryEnvironment) : Rep[Query[(DomainA, DomainB)]] = {
			val mDomA = implicitly[Manifest[DomainA]]
			val mDomB = implicitly[Manifest[DomainB]]
			(qa, qb) match {
				case (a, b) if isGreater(taintOf(a), taintOf(b), env) =>
					//a x b --> b x a
					projection (
						crossProduct(b, a),
						fun(
							(bx : Rep[DomainB], ax : Rep[DomainA]) => make_tuple2(ax, bx)
						)(
							mDomB,
							mDomA,
							manifest[(DomainA, DomainB)]
						)
					)

				case (a, Def(cross@CrossProduct(b, c))) if isGreater(taintOf(a), taintOf(b), env) =>
					//a x (b x c) --> b x (a x c)
					val mA = mDomA
					val mB = cross.mDomA
					val mC = cross.mDomB

					projection(
						crossProduct(b, crossProduct(a, c)),
						fun(
							(b : Rep[_], ac : Rep[(_, _)]) =>
								make_tuple2((tuple2_get1[Any](ac),make_tuple2((b,tuple2_get2[Any](ac)))))
						)(
							mB,
							tupledManifest(mA.asInstanceOf[Manifest[Any]], mC),
							tupledManifest(mB, tupledManifest(mA.asInstanceOf[Manifest[Any]], mC))
						)
					).asInstanceOf[Rep[Query[(DomainA, DomainB)]]]

				case (Def(cross@CrossProduct(a, b)), c)	if isGreater(taintOf(b), taintOf(c), env) =>
					//(a x b) x c --> (a x c) x b
					val mA = cross.mDomA
					val mB = cross.mDomB
					val mC = mDomB

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

				case _ => IR.crossProduct(qa, qb)

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
