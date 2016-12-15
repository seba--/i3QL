package idb.syntax.iql

import idb.query.QueryEnvironment
import idb.query.taint.TaintId
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object DECLASS {

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]],
		taints : String*
	)(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		DECLASS(relation, taints.toSet)

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]],
		taints : Set[String]
	)(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		declassification(relation, taints.map(s => TaintId(s)))
}
