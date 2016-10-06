package idb.algebra

import idb.algebra.normalization.{RelationalAlgebraIRNormalizeSubQueries, RelationalAlgebraIRNormalizeBasicOperators}
import idb.algebra.ir._
import idb.algebra.base.RelationalAlgebraDerivedOperators

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRNormalizePackage
  extends RelationalAlgebraIRNormalizeBasicOperators
  with RelationalAlgebraIRNormalizeSubQueries
