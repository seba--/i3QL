package idb.query.colors


/**
 * @author Mirko Köhler
 */

trait Color {
	def union(other : Color) : Color =
		Color.union(this, other)

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




	/*def union(a : Color, b : Color) : Coloring = (a, b) match {
		case (x, NO_COLORING) => x
		case (NO_COLORING, y) => NO_COLORING

		case (Coloring(xs, xMap),Coloring(ys, yMap)) => Coloring(xs union ys, mapUnion(xMap, yMap))


	}    */

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



/*trait Color {
	def <(other : Color) : Boolean =
		Color.compareTo(this, other) < 0

	def >(other : Color) : Boolean =
		Color.compareTo(this, other) > 0
}




object Color {



	def join(descA : Color, descB : Color) : Color = (descA, descB) match {
		case (CompoundColor(as), CompoundColor(bs)) => CompoundColor(as ++ bs)
		case (CompoundColor(as), b) => CompoundColor(as + b)
		case (a, CompoundColor(bs)) => CompoundColor(bs + a)
		case (a, b) if a == b => a
		case (a, b) => CompoundColor(Set(a, b))
	}

	//0 = equal, -0 = descA smaller, +0 = descA bigger
	def compareTo(descA : Color, descB : Color) : Int = (descA, descB) match {
		case (NoColor, NoColor) => 0
		case (NoColor, _) => -1
		case (_, NoColor) => 1

		case (SingleColor(s1), SingleColor(s2)) =>
			s1 compareTo s2
		case (SingleColor(_), _) => -1
		case (_, SingleColor(_)) => 1

		case (CompoundColor(set1), CompoundColor(set2)) =>
			val setSize = set1.size - set2.size
			if (setSize != 0)
				setSize
			else {
				val set1Min = set1.fold(null)((x1 : Color, x2 : Color) => if (x1 == null) x2 else if (compareTo(x1, x2) > 0) x2 else x1)
				val set2Min = set2.fold(null)((x1 : Color, x2 : Color) => if (x1 == null) x2 else if (compareTo(x1, x2) > 0) x2 else x1)

				(set1Min, set2Min) match {
					case (null, null) => 0
					case (null, _) => -1
					case (_, null) => 1
					case (a, b) =>
						val cmp = compareTo(a, b)
						if (cmp != 0)
							cmp
						else
							compareTo(
								CompoundColor(set1 - set1Min),
								CompoundColor(set2 - set2Min)
							)
				}
			}
	}
}

case object NoColor extends Color
case class SingleColor(s : String) extends Color
case class CompoundColor(descs : Set[Color]) extends Color
       */


