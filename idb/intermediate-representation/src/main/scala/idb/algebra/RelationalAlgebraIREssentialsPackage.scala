package idb.algebra

import idb.lms.extensions.functions.{FunctionsExpDynamicLambda, TupledFunctionsExpDynamicLambda}
import idb.lms.extensions.{FunctionUtils, RemoteUtils}

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
trait RelationalAlgebraIREssentialsPackage
	extends RelationalAlgebraIROperatorsPackage
	with RemoteUtils
	with FunctionUtils
	with TupledFunctionsExpDynamicLambda
