package idb.algebra.remote

import idb.algebra.base.RelationalAlgebraBase

/**
 * @author Mirko Köhler
 */
trait RemoteDescription

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
}

case object DefaultDescription extends RemoteDescription
case class NameDescription(s : String) extends RemoteDescription
case class SetDescription(descs : Set[RemoteDescription]) extends RemoteDescription
