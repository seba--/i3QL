package idb.algebra.remote.placement

import idb.algebra.QueryTransformerAdapter
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.exceptions.{InsufficientRootPermissionsException, NoServerAvailableException}
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.{QueryTaint, StandardQueryTaint}
import idb.lms.extensions.RemoteUtils
import idb.query.taint.Taint
import idb.query.{Host, QueryEnvironment}

protected[remote] trait StandardPlacementTransformer
	extends QueryTransformerAdapter with QueryTaint {

	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RemoteUtils
	import IR._

	override def transform[Domain : Manifest](relation : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Rep[Query[Domain]] = {
		//Predef.println(s"Transforming ~ $relation")

		relation match {

			case Def(Root(q, host)) =>
				val transQ = transform(q)

				//Adds a remote node as child of the root if the relation is on another server
				if (transQ.host != host) {
					val taintQ = taintOf(transQ)
					val rootPermissions = env.permissionsOf(host)

					if (taintQ.ids subsetOf rootPermissions)
						super.transform(root(remote(transQ, host), host))
					else
						throw new InsufficientRootPermissionsException(host.name, rootPermissions, taintQ)
				} else {
					super.transform(root(transQ, host))
				}

			case Def(Reclassification(q, newTaint)) =>
				val transQ = transform(q)

				val host = transQ.host

				if (newTaint.ids subsetOf env.permissionsOf(host))
					super.transform(reclassification(transQ, newTaint))
				else {
					val h = idb.query.findHost(env, newTaint.ids)
					h match {
						case Some(x) =>
							super.transform(reclassification(remote(transQ, x), newTaint))
						case None =>
							throw new NoServerAvailableException(newTaint.ids)
					}
				}

			case Def(CrossProduct(qa, qb)) =>
				super.transform(distributeRelations(transform(qa), transform(qb), (a : Rep[Query[Any]], b : Rep[Query[Any]]) => crossProduct(a, b)).asInstanceOf[Rep[Query[Domain]]])

			case Def(EquiJoin(qa, qb, eqs)) =>
				super.transform(distributeRelations(transform(qa), transform(qb), (a : Rep[Query[Any]], b : Rep[Query[Any]]) => equiJoin(a, b, eqs)).asInstanceOf[Rep[Query[Domain]]])

			case Def(UnionMax(qa, qb)) =>
				super.transform(distributeRelations(transform(qa), transform(qb), (a : Rep[Query[Any]], b : Rep[Query[Any]]) => unionMax(a, b)).asInstanceOf[Rep[Query[Domain]]])

			case Def(UnionAdd(qa, qb)) =>
				super.transform(distributeRelations(transform(qa), transform(qb), (a : Rep[Query[Any]], b : Rep[Query[Any]]) => unionAdd(a, b)).asInstanceOf[Rep[Query[Domain]]])

			case Def(Intersection(qa, qb)) =>
				super.transform(distributeRelations(transform(qa), transform(qb), (a : Rep[Query[Any]], b : Rep[Query[Any]]) => intersection(a, b)).asInstanceOf[Rep[Query[Domain]]])

			case Def(Difference(qa, qb)) =>
				super.transform(distributeRelations(transform(qa), transform(qb), (a : Rep[Query[Any]], b : Rep[Query[Any]]) => difference(a, b)).asInstanceOf[Rep[Query[Domain]]])

			case _ =>
				super.transform(pushTransform(relation))
		}
	}


	/**
	  * If true, then a new server will be found according to higher priority rather than if the
	  * operator is already on the server.
	  */
	private val PRIORITY_OVER_SAME_HOST = true

	def distributeRelations[DomainA : Manifest, DomainB : Manifest, Range : Manifest](
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		constructor : (Rep[Query[DomainA]], Rep[Query[DomainB]]) => Rep[Query[Range]]
	)(implicit env: QueryEnvironment) : Rep[Query[Range]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB = implicitly[Manifest[DomainB]]

		val hostA : Host = relationA.host
		val hostB : Host = relationB.host
		val taintA : Taint = taintOf(relationA)
		val taintB : Taint = taintOf(relationB)


		//The hosts are the same -> No need for distribution
		if (hostA == hostB)
			return constructor(relationA, relationB)

		val allTaints = taintA.ids union taintB.ids
		val oldHost = idb.query.findHost(env, scala.collection.Seq(hostA, hostB), allTaints)
		val newHost = idb.query.findHost(env, allTaints)

		import env._
		newHost match {
			case Some(h) =>
				oldHost match {
					case Some(a) if a == hostA && (!PRIORITY_OVER_SAME_HOST || priorityOf(a) <= priorityOf(h)) =>
						return transform(constructor(relationA, remote(relationB, hostA)))
					case Some(b) if b == hostB && (!PRIORITY_OVER_SAME_HOST || priorityOf(b) <= priorityOf(h)) =>
						return transform(constructor(remote(relationA, hostB), relationB))
					case _ =>
						return transform(constructor(remote(relationA, h), remote(relationB, h)))
				}
			case None =>
				throw new NoServerAvailableException(allTaints)
		}
	}

}
