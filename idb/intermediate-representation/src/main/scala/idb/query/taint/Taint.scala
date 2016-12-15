package idb.query.taint


/**
 * @author Mirko Köhler
 */

trait Taint {
	def union(other : Taint) : Taint =
		Taint.union(this, other)

  def -(taint : Set[TaintId]) : Taint =
    Taint.diff(this, taint)

	def ids : Set[TaintId]
}


case class BaseTaint(ids : Set[TaintId]) extends Taint
case class RecordTaint(fields : Map[FieldId, Taint]) extends Taint {
	def ids : Set[TaintId] =
		fields.values.map(_.ids).fold(Set.empty[TaintId])((a, b) => a ++ b)
}

object Taint {

	val NO_TAINT = BaseTaint(Set.empty)

	def empty = group()

	def apply(name : String) : Taint =
		group(name)

	def group(names : String*) : Taint =
		BaseTaint(names.map(TaintId).toSet)

	def apply(es : (String, Taint)*) : Taint =
		if (es.isEmpty)
			empty
		else
			RecordTaint(Map(es.map(t => (FieldName(t._1), t._2)) : _*))

	/**
	 * Creates a taint that colors a tuple, where each element in the tuple has the corresponding taint given as parameter.
	 */
	def tupled(taints : Taint*) : Taint = {
		val indexedTaints = taints.zipWithIndex.map(t => (t._2 + 1, t._1))
		val fieldMap = Map[FieldId, Taint](indexedTaints.map(t => (FieldName(s"_${t._1}"), t._2)) : _*)
		RecordTaint(fieldMap)
	}

	def ids(taints : Set[Taint]) : Set[TaintId] =
		taints.flatMap(_.ids)

	def toBaseTaint(taints : Set[Taint]) : Taint =
		BaseTaint(ids(taints))

	def union(col1 : Taint, col2 : Taint) : Taint = (col1, col2) match {
		case (BaseTaint(as), BaseTaint(bs)) =>
			BaseTaint(as ++ bs)

		case (a@BaseTaint(as), RecordTaint(mb)) =>
			RecordTaint(mb.map(t => (t._1, union(a, t._2))))

		case (RecordTaint(ma), b@BaseTaint(bs)) =>
			RecordTaint(ma.map(t => (t._1, union(t._2, b))))

		case (RecordTaint(ma), RecordTaint(mb)) if ma.size == mb.size && ma.keySet == mb.keySet =>
			RecordTaint(ma ++ mb.map(t => t._1 -> union(ma(t._1), t._2)))
	}

	def diff(col : Taint, taint : Set[TaintId]) : Taint = col match {
		case BaseTaint(as) =>
			BaseTaint(as -- taint)
		case RecordTaint(m) =>
			RecordTaint(m.map(t => (t._1, diff(t._2, taint))))
	}


	private def mapUnion[A,B](a : Map[A,Set[B]], b : Map[A,Set[B]]) : Map[A,Set[B]] = {
		if (a.isEmpty)
			b
		else if (b.isDefinedAt(a.head._1)) {
			mapUnion(a - a.head._1, b - a.head._1) + ((a.head._1, a.head._2.union(b(a.head._1))))
		} else {
			mapUnion(a - a.head._1, b) + a.head
		}


	}
}