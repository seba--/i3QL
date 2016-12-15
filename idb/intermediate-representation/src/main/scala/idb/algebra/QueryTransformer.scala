package idb.algebra

import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.query.QueryEnvironment

protected[algebra] trait QueryTransformer {

	val IR : RelationalAlgebraBase
	import IR._

	def transform[Domain : Manifest](relation : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		throw new NoTransformationAvailableException
}

protected[algebra] trait QueryTransformerAdapter
	extends QueryTransformer {

	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
	import IR._

	override def transform[Domain : Manifest](relation : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Rep[Query[Domain]] = {
		relation match {
			//Base
			case QueryRelation(_, _, _, _) => relation
			case QueryTable(_, _, _, _) => relation
			case Def (Root(q, host)) => root(transform(q), host)
			case Def (Materialize(q)) => materialize(transform(q))

			//Basic operators
			case Def (Projection(q, f)) => projection(transform(q), f)
			case Def (Selection(q, f)) => selection(transform(q), f)
			case Def (CrossProduct(qa, qb)) => crossProduct(transform(qa), transform(qb)).asInstanceOf[Rep[Query[Domain]]]
			case Def (EquiJoin(qa, qb, eqs)) => equiJoin(transform(qa), transform(qb), eqs).asInstanceOf[Rep[Query[Domain]]]
			case Def (DuplicateElimination(q)) => duplicateElimination(transform(q))
			case Def (Unnest(q, f)) => unnest(transform(q), f).asInstanceOf[Rep[Query[Domain]]]

			//Set theory operators
			case Def (UnionAdd(qa, qb)) => unionAdd(transform(qa), transform(qb))
			case Def (UnionMax(qa, qb)) => unionMax(transform(qa), transform(qb))
			case Def (Intersection(qa, qb)) => intersection(transform(qa), transform(qb))
			case Def (Difference(qa, qb)) => difference(transform(qa), transform(qb))

			//Aggregation operators
			case Def (AggregationSelfMaintained(q, gr, start, fa, fr, fu, ck, conv)) =>
				aggregationSelfMaintained(transform(q), gr, start, fa, fr, fu, ck, conv)
			case Def (AggregationNotSelfMaintained(q, gr, start, fa, fr, fu, ck, conv)) =>
				aggregationNotSelfMaintained(transform(q), gr, start, fa, fr, fu, ck, conv)

			//Remote Operators
			case Def (Remote(q, host)) => remote(transform(q), host)
			case Def (Reclassification(q, newTaint)) => reclassification(transform(q), newTaint)
			case Def (Declassification(q, taintIds)) => declassification(transform(q), taintIds)
			case Def (ActorDef(_, _, _)) => relation

			case _ => super.transform(relation)
		}
	}


}

class NoTransformationAvailableException
	extends RuntimeException("No transformations available for the query.")
