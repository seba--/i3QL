package idb.algebra

import idb.algebra.normalization.{RelationalAlgebraIRNormalizeSubQueries, RelationalAlgebraIRNormalizeBasicOperators}
import idb.algebra.ir._
import idb.algebra.base.RelationalAlgebraDerivedOperators

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIROperatorsPackage
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSetTheoryOperators
    with RelationalAlgebraIRRecursiveOperators
    with RelationalAlgebraIRAggregationOperators
    with RelationalAlgebraIRSubQueries
    with RelationalAlgebraIRMultiRelations
	with RelationalAlgebraIRRemoteOperators
    with RelationalAlgebraDerivedOperators
