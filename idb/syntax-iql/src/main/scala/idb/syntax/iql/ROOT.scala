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
	)(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] =
		apply(relation, Host.local)

	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]],
		host : Host
	)(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] = {
		val q = root(relation, host)
		val r = compile(q)
		//LinkActor.forward(queryEnvironment.system, r)
		r
	}

}
