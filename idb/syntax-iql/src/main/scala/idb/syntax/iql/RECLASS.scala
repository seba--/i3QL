package idb.syntax.iql

import idb.query.QueryEnvironment
import idb.query.taint.Taint
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object RECLASS {

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]],
		newTaint : Taint
	)(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		reclassification(relation, newTaint)
}
