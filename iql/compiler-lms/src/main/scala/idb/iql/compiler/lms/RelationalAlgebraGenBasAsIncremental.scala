package idb.iql.compiler.lms

import scala.virtualization.lms.internal.ScalaCodegen

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBasAsIncremental
    extends ScalaCodegen
{

    val IR: RelationalAlgebraIRBase

    import IR._


//    def emitInc[Domain](sym: Sym[Any], rhs: Def[Any]):  Relation[Domain] = {
//        rhs match {
//            case Def(BaseRelation (relImpl)) => relImpl
//        }
//    }


}
