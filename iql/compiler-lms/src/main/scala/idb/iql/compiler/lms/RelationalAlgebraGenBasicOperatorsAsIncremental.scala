package idb.iql.compiler.lms

import scala.virtualization.lms.common.{CompileScala, ScalaOpsPkgExp, ScalaGenBase, ScalaGenEffect}

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBasicOperatorsAsIncremental
    extends RelationalAlgebraGenBaseAsIncremental
    with ScalaGenBase
    with ScalaGenEffect
    with CompileScala
{

    val IR: RelationalAlgebraIRBasicOperators with ScalaOpsPkgExp

    import IR._

    def compile[Domain](exp: Rep[Relation[Domain]]): IR.CompiledRelation[Domain] = exp match {
        case Def (IR.Selection (r, f)) =>
            new sae.operators.impl.SelectionView (compile (r), compile (f))
        case Def (IR.Projection (r, f)) =>
            new sae.operators.impl.ProjectionSetRetainingView (compile (r), compile (f))
        case _ => super.compile (exp)
    }

}
