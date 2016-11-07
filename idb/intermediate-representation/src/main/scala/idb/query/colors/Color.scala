package idb.query.colors


/**
 * @author Mirko Köhler
 */

trait Color {
	def union(other : Color) : Color =
		Color.union(this, other)

  def -(taint : Set[ColorId]) : Color =
    Color.diff(this, taint)

	def ids : Set[ColorId]
}


case class ClassColor(ids : Set[ColorId]) extends Color
case class FieldColor(fields : Map[FieldId, Color]) extends Color {
	def ids : Set[ColorId] =
		fields.values.map(_.ids).fold(Set.empty[ColorId])((a, b) => a ++ b)
}



object Color {

	val NO_COLOR = ClassColor(Set.empty)

	def empty = group()

	def apply(name : String) : Color =
		group(name)

	def group(names : String*) : Color =
		ClassColor(names.map(StringColor).toSet)

	def apply(es : (String, Color)*) : Color =
		if (es.isEmpty)
			empty
		else
			FieldColor(Map(es.map(t => (FieldName(t._1), t._2)) : _*))

	/**
	 * Creates a color that colors a tuple, where each element in the tuple has the corresponding color given as parameter.
	 */
	def tupled(cols : Color*) : Color = {
		val indexedColors = cols.zipWithIndex.map(t => (t._2 + 1, t._1))
		val fieldMap = Map[FieldId, Color](indexedColors.map(t => (FieldName(s"_${t._1}"), t._2)) : _*)
		FieldColor(fieldMap)
	}

	def ids(colors : Set[Color]) : Set[ColorId] =
		colors.flatMap(_.ids)

	def fromIdsInColors(colors : Set[Color]) : Color =
		ClassColor(ids(colors))

	def union(col1 : Color, col2 : Color) : Color = (col1, col2) match {
		case (ClassColor(as), ClassColor(bs)) =>
			ClassColor(as ++ bs)

		case (a@ClassColor(as), FieldColor(mb)) =>
			FieldColor(mb.map(t => (t._1, union(a, t._2))))

		case (FieldColor(ma), b@ClassColor(bs)) =>
			FieldColor(ma.map(t => (t._1, union(t._2, b))))

		case (FieldColor(ma), FieldColor(mb)) if ma.size == mb.size && ma.keySet == mb.keySet =>
			FieldColor(ma ++ mb.map(t => t._1 -> union(ma(t._1), t._2)))
	}

  def diff(col : Color, taint : Set[ColorId]) : Color = col match {
    case ClassColor(as) =>
      ClassColor(as -- taint)
    case FieldColor(m) =>
      FieldColor(m.map(t => (t._1, diff(t._2, taint))))
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