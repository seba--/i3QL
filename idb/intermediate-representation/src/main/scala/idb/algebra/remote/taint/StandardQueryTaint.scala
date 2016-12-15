package idb.algebra.remote.taint

import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.lms.extensions.RemoteUtils
import idb.query.taint.{BaseTaint, Taint}

protected[remote] trait StandardQueryTaint extends QueryTaint {

	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
	import IR._

	override def taintOf(relation : Rep[Query[_]]) : Taint = relation match {
		//Base
		case QueryRelation(_, _, taint, _) => taint
		case QueryTable(_, _, taint, _) => taint
		case Def (Root(q, host)) => taintOf(q)
		case Def (Materialize(q)) => taintOf(q)

		//Basic operators
		case Def (Projection(q, f)) => taintOfProjection(taintOf(q), f)
		case Def (Selection(q, f)) =>
			val taintQ = taintOf(q)
			Taint.union(taintQ, Taint.toBaseTaint(taintOfBase(f, taintQ)))
		case Def (CrossProduct(qa, qb)) => Taint.tupled(taintOf(qa), taintOf(qb))
		case Def (EquiJoin(qa, qb, eqs)) =>
			val taintA = taintOf(qa)
			val taintB = taintOf(qb)
			val e = eqs.foldRight(Predef.Set.empty[Taint])((a, b) => taintOfBase(a._1, taintA))
			val f = eqs.foldRight(Predef.Set.empty[Taint])((a, b) => taintOfBase(a._2, taintB))

			val eUnionF = Taint.union(Taint.toBaseTaint(e), Taint.toBaseTaint(f))
			Taint.tupled(Taint.union(taintA, eUnionF), Taint.union(taintB, eUnionF))
		case Def (DuplicateElimination(q)) => taintOf(q)
		case Def (Unnest(q, f)) =>
			val taintQ = taintOf(q)
			Taint.tupled(taintQ, Taint.toBaseTaint(taintOfBase(f, taintQ)))

		//Set theory operators
		case Def (UnionAdd(qa, qb)) => taintOf(qa) union taintOf(qb)
		case Def (UnionMax(qa, qb)) => taintOf(qa) union taintOf(qb)
		case Def (Intersection(qa, qb)) => taintOf(qa) union taintOf(qb)
		case Def (Difference(qa, qb)) => taintOf(qa) union taintOf(qb)

		//Aggregation operators
		case Def (AggregationSelfMaintained(q, group, start, added, removed, updated, convertKey, convert)) =>
			val taintQ = taintOf(q)
			val cGroup = taintOfBase(group, taintQ)
			val cConvertKey = taintOfBase(convertKey, Taint.toBaseTaint(cGroup))
			val rangeACol = Taint.toBaseTaint(cConvertKey)

			val cAdd = taintOfBase(added, Taint.tupled(taintQ, Taint.NO_TAINT))
			val cRemove = taintOfBase(removed, Taint.tupled(taintQ, Taint.NO_TAINT))
			val cUpdate = taintOfBase(updated, Taint.tupled(taintQ, taintQ, Taint.NO_TAINT))
			val rangeBCol = Taint.toBaseTaint(cAdd ++ cRemove ++ cUpdate)

			val cConvert = taintOfBase(convert, Taint.tupled(rangeACol, rangeBCol, taintQ))

			Taint.toBaseTaint(cConvert)

		case Def (AggregationNotSelfMaintained(q, group, start, added, removed, updated, convertKey, convert)) =>
			val taintQ = taintOf(q)
			val cGroup = taintOfBase(group, taintQ)
			val cConvertKey = taintOfBase(convertKey, Taint.toBaseTaint(cGroup))
			val rangeACol = Taint.toBaseTaint(cConvertKey)

			val cAdd = taintOfBase(added, Taint.tupled(taintQ, Taint.NO_TAINT, BaseTaint(taintQ.ids)))
			val cRemove = taintOfBase(removed, Taint.tupled(taintQ, Taint.NO_TAINT, BaseTaint(taintQ.ids)))
			val cUpdate = taintOfBase(updated, Taint.tupled(taintQ, taintQ, Taint.NO_TAINT, BaseTaint(taintQ.ids)))
			val rangeBCol = Taint.toBaseTaint(cAdd ++ cRemove ++ cUpdate)

			val cConvert = taintOfBase(convert, Taint.tupled(rangeACol, rangeBCol, taintQ))

			Taint.toBaseTaint(cConvert)

		//Remote Operators
		case Def (Remote(q, host)) => taintOf(q)
		case Def (Reclassification(q, newTaint)) => newTaint
		case Def (Declassification(q, taintIds)) => taintOf(q) - taintIds
		case Def (ActorDef(_, _, taint)) => taint

		case _ => super.taintOf(relation)
	}
}
