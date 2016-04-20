package idb.syntax.iql

import idb.query.colors.Color
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object RECLASS {

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]] ,
		newColor : Color
	) : Rep[Query[Domain]] =
		reclassification(relation, newColor)
}
