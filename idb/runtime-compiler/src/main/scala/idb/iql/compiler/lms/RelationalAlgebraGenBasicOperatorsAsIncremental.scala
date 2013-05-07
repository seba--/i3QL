package idb.iql.compiler.lms

import idb.iql.lms.extensions.CompileScalaExt
import scala.virtualization.lms.common.{FunctionsExp, ScalaGenEffect}

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBasicOperatorsAsIncremental
    extends RelationalAlgebraGenBaseAsIncremental
    with CompileScalaExt with ScalaGenEffect
{

    val IR: RelationalAlgebraIRBasicOperators with RelationalAlgebraGenSAEBinding with FunctionsExp

    // TODO incorporate set semantics into ir
    override def compile[Domain: Manifest] (exp: IR.Rep[IR.Relation[Domain]]): idb.Relation[Domain] = exp match {
        case IR.Def (IR.Selection (r, f)) =>
            new idb.operators.impl.SelectionView (compile (r), compileApplied (f), false)
        case IR.Def (IR.Projection (r, f)) =>
            new idb.operators.impl.ProjectionView (compile (r), compileApplied (f), false)
        case _ => super.compile (exp)
    }

}
