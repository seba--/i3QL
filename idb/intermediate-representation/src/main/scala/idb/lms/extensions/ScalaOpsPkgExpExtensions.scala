package idb.lms.extensions

import idb.lms.extensions.equivalence.{StructExpAlphaEquivalence, TupledFunctionsExpAlphaEquivalence}
import idb.lms.extensions.functions.TupledFunctionsExpDynamicLambda
import idb.lms.extensions.operations._

import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp}

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
trait ScalaOpsPkgExpExtensions extends ScalaOpsPkgExp
	with ScalaOpsPkgExpOptExtensions
	with StaticDataExp
	with DateOpsExp
	with OptionOpsExp
	with EitherOpsExp
	with SeqOpsExpExt
	with SetOpsExpExt
	with StringOpsExpExt
	with StructExpExt
	with TupledFunctionsExpDynamicLambda