package idb.iql.compiler.lms

import scala.virtualization.lms.internal.ScalaCodegen

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraScalaGen
  extends ScalaCodegen
{

  val IR: RelationalAlgebraIRBase
  import IR._


  override def emitNode(sym: Sym[Any], rhs: Def[Any]): Unit = {
    rhs match {
      case BaseRelation() => throw new UnsupportedOperationException()//emitValDef(sym, "")
    }
  }

}
