package idb.syntax.iql

import idb.query.Color
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object RECLASS {

	def apply[Domain : Manifest](
		newColor : Color,
		relation : Rep[Query[Domain]]
	) : Rep[Query[Domain]] =
		reclassification(relation, newColor)
}
