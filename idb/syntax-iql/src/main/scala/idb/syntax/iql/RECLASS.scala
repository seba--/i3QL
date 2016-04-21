package idb.syntax.iql

import idb.query.QueryEnvironment
import idb.query.colors.Color
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object RECLASS {

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]] ,
		newColor : Color
	)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] =
		reclassification(relation, newColor)
}
