package idb.query

/**
 * @author Mirko Köhler
 */
trait Color {
	def <(other : Color) : Boolean =
		Color.compareTo(this, other) < 0

	def >(other : Color) : Boolean =
		Color.compareTo(this, other) > 0
}

object Color {

	def apply(name : String) : Color =
		SingleColor(name)

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
