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

  override def compile[Domain: Manifest] (exp: IR.Rep[IR.Relation[Domain]]): idb.Relation[Domain] = exp match {
    case IR.Def (IR.Selection (r, f)) =>
      // TODO do something about anyrefs to fix the ugly typecasts
      new idb.operators.impl.SelectionView (compile (r).asInstanceOf[idb.Relation[Domain with AnyRef]],
        compileApplied (f)).asInstanceOf[idb.Relation[Domain]]
    case IR.Def (IR.Projection (r, f)) =>
      new idb.operators.impl.ProjectionSetRetainingView (compile (r), compileApplied (f))
    case _ => super.compile (exp)
  }

}
