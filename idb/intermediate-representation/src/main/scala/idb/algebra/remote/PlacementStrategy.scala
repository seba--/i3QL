package idb.algebra.remote

import idb.algebra.RelationalAlgebraIROperatorsPackage
import idb.algebra.remote.placement.StandardPlacementTransformer
import idb.algebra.remote.taint.StandardQueryTaint

trait PlacementStrategy extends StandardPlacementTransformer with StandardQueryTaint {
	val IR : RelationalAlgebraIROperatorsPackage
}
