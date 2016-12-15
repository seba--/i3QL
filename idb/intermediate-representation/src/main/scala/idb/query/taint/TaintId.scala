package idb.query.taint

/**
 * @author Mirko Köhler
 */
case class TaintId(name : String) {

	override def toString: String =
		s"<$name>"
}
