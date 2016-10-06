package idb.syntax.iql

import idb.query.QueryEnvironment
import idb.query.colors.{Color, StringColor}
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object DECLASS {

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]] ,
		colors : Set[String]
	)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] =
		declassification(relation, colors.map(s => StringColor(s)))
}
