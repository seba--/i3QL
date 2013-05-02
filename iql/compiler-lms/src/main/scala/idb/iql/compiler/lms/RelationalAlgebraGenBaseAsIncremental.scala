package idb.iql.compiler.lms

import scala.virtualization.lms.common.{ScalaOpsPkgExp, ScalaGenBase, ScalaGenEffect}

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBaseAsIncremental
{

    val IR: RelationalAlgebraIRBase with ScalaOpsPkgExp

    import IR._

    def compile[Domain](exp: Rep[Relation[Domain]]): IR.CompiledRelation[Domain] = exp match {
        case BaseRelation (rel: IR.CompiledRelation[Domain]) => rel
        //case Def (Selection (selectE, f)) => new sae.operators.impl.SelectionView (compile (selectE)(rel), f)
    }

}
