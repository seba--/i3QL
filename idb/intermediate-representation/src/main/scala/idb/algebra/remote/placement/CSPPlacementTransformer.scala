package idb.algebra.remote.placement

import idb.algebra.QueryTransformerAdapter
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.RemoteUtils
import idb.query.QueryEnvironment

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
trait CSPPlacementTransformer
	extends QueryTransformerAdapter with QueryTaint {


	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RemoteUtils

	import IR._

	override def transform[Domain: Manifest](relation: Rep[Query[Domain]])(implicit env: QueryEnvironment): Rep[Query[Domain]] = {
		//Predef.println(s"Transforming ~ $relation")

		val nodes = env.hosts
		null

	}

}
