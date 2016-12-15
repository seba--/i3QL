package idb.algebra

import idb.algebra.normalization.{RelationalAlgebraIRNormalizeBasicOperators, RelationalAlgebraIRNormalizeSubQueries}
import idb.algebra.ir._
import idb.algebra.base.RelationalAlgebraDerivedOperators
import idb.lms.extensions.{FunctionUtils, RemoteUtils}

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

