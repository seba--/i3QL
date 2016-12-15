package idb.algebra.remote.taint

import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.exceptions.NoTaintAvailableException
import idb.query.taint.Taint

trait QueryTaint {
	val IR : RelationalAlgebraBase
	import IR._

	def taintOf(relation : Rep[Query[_]]) : Taint =
		throw new NoTaintAvailableException
}
