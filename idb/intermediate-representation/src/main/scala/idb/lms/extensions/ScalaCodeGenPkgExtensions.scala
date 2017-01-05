package idb.lms.extensions

import idb.lms.extensions.operations._

import scala.virtualization.lms.common._

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
trait ScalaCodeGenPkgExtensions extends ScalaCodeGenPkg
	with ScalaGenStaticData
	with ScalaGenOptionOps
	with ScalaGenEitherOps
	with ScalaGenStringOpsExt
	with ScalaGenSeqOpsExt
	with ScalaGenStruct
	with ScalaGenTupledFunctions
	with ScalaGenDateOps
	with ScalaGenEqual
{
	override val IR : ScalaOpsPkgExpExtensions
}
