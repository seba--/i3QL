package idb.iql.lms.extensions

import scala.virtualization.lms.common.FunctionsExp
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 */
trait FunctionsExpOptBetaReduction
  extends FunctionsExp
{

  override def doApply[A: Manifest, B: Manifest] (f: Exp[A => B], x: Exp[A])(implicit pos: SourceContext): Exp[B] =
  {
    val x1 = unbox (x)
    f match {
      case Def (Lambda (g, _, y)) =>
        // if function result is known to be pure, so is application
        // TODO: what about
        //val ye = summarizeEffects(y)
        //reflectEffect(Apply(f, x1), ye)
        g (x1)
      case _ => // unknown function, assume it is effectful TODO: global vs simple?
        reflectEffect (Apply (f, x1))
    }
  }

}
