package idb.syntax.iql

import idb.query.{Host, QueryEnvironment}
import idb.query.colors.Color
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object ROOT {

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]]
	)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] =
		apply(relation, Host.local)

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]],
		host : Host
	)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] =
		root(relation, host)
}
