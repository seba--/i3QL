package idb.iql.lms.extensions

import scala.virtualization.lms.common.{EqualExpOpt, OrderingOpsExp, NumericOpsExpOpt}
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 */
trait ScalaOpsExpConstantPropagation
    extends NumericOpsExpOpt with OrderingOpsExp with EqualExpOpt
{

    //  override def equals[A: Manifest, B: Manifest] (a: Rep[A], b: Rep[B])
    //      (implicit pos: SourceContext): Rep[Boolean] =
    //    if (a == b)
    //      Const (true)
    //    else
    //    {
    //      (a, b) match
    //      {
    //        case (Const (true), b: DefMN[B, Boolean]) => b
    //        case (a: DefMN[A, Boolean], Const (true)) => a
    //        case _ => super.equals (a, b)
    //      }
    //    }


    override def numeric_plus[T: Numeric : Manifest] (lhs: Exp[T], rhs: Exp[T])
                                                     (implicit pos: SourceContext): Exp[T] =
        (lhs, rhs) match {
            // e.g.,  (a + 1) + 2 := a + 3
            case (Def (NumericPlus (x, Const (v1))), Const (v2)) =>
                numeric_plus (x, Const (implicitly[Numeric[T]].plus (v1, v2)))
            // e.g.,  (1 + a) + 2 := a + 3
            case (Def (NumericPlus (Const (v1), x)), Const (v2)) =>
                numeric_plus (x, Const (implicitly[Numeric[T]].plus (v1, v2)))
            // super optimizations
            case _ => super.numeric_plus (lhs, rhs)
        }

    override def ordering_gt[T] (lhs: Exp[T], rhs: Exp[T])(
        implicit evidenceOrd: scala.math.Ordering[T],
        evidenceMan: scala.reflect.Manifest[T],
        pos: scala.reflect.SourceContext
    ): Rep[Boolean] =
        (lhs, rhs) match {
            // e.g., x + 2 > 0 := x > -2
            case (Def (NumericPlus (x, Const (v1))), Const (v2)) =>
                ordering_gt (x, Const (numericEvidenceFromOrdering.minus (v2, v1)))
            case _ => super.ordering_gt (lhs, rhs)
        }

    // TODO make an option
    def numericEvidenceFromOrdering[T] (implicit ord: Ordering[T]): Numeric[T] = {
        ord match {
            case Ordering.Int => Numeric.IntIsIntegral
            case _ => null
        }
    }
}
