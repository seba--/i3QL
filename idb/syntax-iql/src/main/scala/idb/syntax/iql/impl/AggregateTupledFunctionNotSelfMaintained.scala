package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateTupledFunctionNotSelfMaintained[Select : Manifest, Domain : Manifest, Range : Manifest](
	start : Any,
	added : Rep[((Domain, Any, Iterable[Domain])) => Any],
	removed : Rep[((Domain, Any, Iterable[Domain])) => Any],
	updated : Rep[((Domain, Domain, Any, Iterable[Domain])) => Any],
	project : Rep[Select => Any]
) extends AGGREGATE_TUPLED_FUNCTION_NOT_SELF_MAINTAINED[Select, Domain, Any, Any, Range]
{

	def convert : Rep[((Any, Any)) => Range] =
		(x : Rep[(Any, Any)]) => x.asInstanceOf[Rep[Range]]




}
