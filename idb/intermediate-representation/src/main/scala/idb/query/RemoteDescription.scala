package idb.query

/**
 * @author Mirko Köhler
 */
trait RemoteDescription {
	def <(other : RemoteDescription) : Boolean =
		RemoteDescription.compareTo(this, other) < 0

	def >(other : RemoteDescription) : Boolean =
		RemoteDescription.compareTo(this, other) > 0

	//Unique identifier of the description
	def identifier : List[String]
}

object RemoteDescription {

	def apply(name : String) : RemoteDescription =
		NameDescription(name)

	def join(descA : RemoteDescription, descB : RemoteDescription) : RemoteDescription = (descA, descB) match {
		case (SetDescription(as), SetDescription(bs)) => SetDescription(as ++ bs)
		case (SetDescription(as), b) => SetDescription(as + b)
		case (a, SetDescription(bs)) => SetDescription(bs + a)
		case (a, b) if a == b => a
		case (a, b) => SetDescription(Set(a, b))
	}

	//0 = equal, -0 = descA smaller, +0 = descA bigger
	def compareTo(descA : RemoteDescription, descB : RemoteDescription) : Int = (descA, descB) match {
		case (DefaultDescription, DefaultDescription) => 0
		case (DefaultDescription, _) => -1
		case (_, DefaultDescription) => 1

		case (NameDescription(s1), NameDescription(s2)) =>
			s1 compareTo s2
		case (NameDescription(_), _) => -1
		case (_, NameDescription(_)) => 1

		case (SetDescription(set1), SetDescription(set2)) =>
			val setSize = set1.size - set2.size
			if (setSize != 0)
				setSize
			else {
				val set1Min = set1.fold(null)((x1 : RemoteDescription, x2 : RemoteDescription) => if (x1 == null) x2 else if (compareTo(x1, x2) > 0) x2 else x1)
				val set2Min = set2.fold(null)((x1 : RemoteDescription, x2 : RemoteDescription) => if (x1 == null) x2 else if (compareTo(x1, x2) > 0) x2 else x1)

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
								SetDescription(set1 - set1Min),
								SetDescription(set2 - set2Min)
							)
				}
			}
	}
}

case object DefaultDescription extends RemoteDescription {
	val identifier : List[String] = Nil
}
case class NameDescription(s : String) extends RemoteDescription {
	val identifier = List(s)
}
case class SetDescription(descs : Set[RemoteDescription]) extends RemoteDescription {
	val identifier : List[String] = Nil
}
