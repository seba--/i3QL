package idb.iql.lms.extensions

import scala.virtualization.lms.common.NumericOpsExpOpt
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 */
trait NumericOpsExpOptExtensions
  extends NumericOpsExpOpt
{
  override def numeric_plus[T: Numeric : Manifest] (lhs: Exp[T], rhs: Exp[T])
      (implicit pos: SourceContext): Exp[T] =
    (lhs, rhs) match {
      // constant folding, e.g.,  (a + 1) + 2 = a + 3
      case (Def (NumericPlus (e, Const (x))), Const (y)) => numeric_plus (e, Const (implicitly[Numeric[T]].plus (x, y)))
      // constant folding, e.g.,  (1 + a) + 2 = a + 3
      case (Def (NumericPlus (Const (x), e)), Const (y)) => numeric_plus (e, Const (implicitly[Numeric[T]].plus (x, y)))
      // normalization a + (b + c) = b + c + a
      case (e1, Def (NumericPlus (e2, e3))) => numeric_plus (numeric_plus (e1, e2), e3)
      // super optimizations
      case _ => super.numeric_plus (lhs, rhs)
    }
}
