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


//  override def emitNode(sym: Sym[Any], rhs: Def[Any]) {
//    rhs match {
//      case Def(BaseRelation(_)) => throw new UnsupportedOperationException()//emitValDef(sym, "")
//    }
//  }

}
