package idb.algebra.remote

import idb.algebra.{RelationalAlgebraIREssentialsPackage, RelationalAlgebraIROperatorsPackage}
import idb.algebra.remote.opt.StandardRemoteOptimization
import idb.algebra.remote.placement.{CSPPlacementTransformer, StandardPlacementTransformer}
import idb.algebra.remote.taint.StandardQueryTaint

trait PlacementStrategy
	extends StandardPlacementTransformer
	with StandardQueryTaint
	//with StandardRemoteOptimization
{
	val IR : RelationalAlgebraIREssentialsPackage
}
