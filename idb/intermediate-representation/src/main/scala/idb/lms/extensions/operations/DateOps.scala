package idb.lms.extensions.operations

import java.util.Date

import idb.lms.extensions.equivalence.BaseExpAlphaEquivalence

import scala.language.implicitConversions
import scala.reflect.SourceContext
import scala.virtualization.lms.common.{Base, OrderingOpsExp, ScalaGenBase}

/**
 * Created by seba on 11/10/14.
 */
trait DateOps extends Base {
  implicit def repDateToDateOps(d: Rep[Date]) = new DateOpsCls(d)

  class DateOpsCls(d: Rep[Date]) {
    def getTime = date_getTime(d)
    def compareTo(d2: Rep[Date]) = date_compareTo(d, d2)
    def <(d2: Rep[Date]) = date_lt(d, d2)
    def <=(d2: Rep[Date]) = date_le(d, d2)
    def ==(d2: Rep[Date]) = date_eq(d, d2)
    def >=(d2: Rep[Date]) = date_ge(d, d2)
    def >(d2: Rep[Date]) = date_gt(d, d2)
  }

  def date_getTime(d: Rep[Date])(implicit pos: SourceContext): Rep[Long]
  def date_compareTo(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext): Rep[Int]
  def date_lt(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext): Rep[Boolean]
  def date_le(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext): Rep[Boolean]
  def date_eq(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext): Rep[Boolean]
  def date_ge(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext): Rep[Boolean]
  def date_gt(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext): Rep[Boolean]
}

trait DateOpsExp extends DateOps with BaseExpAlphaEquivalence with OrderingOpsExp {

  case class DateGetTime(d: Exp[Date]) extends Def[Long]
  case class DateCompareTo(d1: Exp[Date], d2: Exp[Date]) extends Def[Int]
  case class DateLT(d1: Exp[Date], d2: Exp[Date]) extends Def[Int]

  def date_getTime(d: Rep[Date])(implicit pos: SourceContext) = DateGetTime(d)
  def date_compareTo(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext) = DateCompareTo(d1, d2)
  def date_lt(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext) = ordering_lt(DateCompareTo(d1, d2), Const(0))
  def date_le(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext) = ordering_lteq(DateCompareTo(d1, d2), Const(0))
  def date_eq(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext) = ordering_equiv(DateCompareTo(d1, d2), Const(0))
  def date_ge(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext) = ordering_gteq(DateCompareTo(d1, d2), Const(0))
  def date_gt(d1: Rep[Date], d2: Rep[Date])(implicit pos: SourceContext) = ordering_gt(DateCompareTo(d1, d2), Const(0))

  override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
    (a, b) match {
      case (DateGetTime(d1), DateGetTime(d2)) => isEquivalent(d1, d2)
      case (DateCompareTo(d11, d12), DateCompareTo(d21, d22)) => isEquivalent(d11, d21) && isEquivalent(d12, d22)
      case _ => super.isEquivalentDef(a, b)
    }

  override def mirror[A:Manifest](e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
    case DateGetTime(d) => date_getTime(f(d))
    case DateCompareTo(d1, d2) => date_compareTo(f(d1), f(d2))
    case _ => super.mirror(e,f)
  }).asInstanceOf[Exp[A]]

}

trait ScalaGenDateOps extends ScalaGenBase {
  val IR: DateOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case DateGetTime(d) => emitValDef(sym, src"$d.getTime")
    case DateCompareTo(d1, d2) => emitValDef(sym, src"$d1.compareTo($d2)")
    case _ => super.emitNode(sym, rhs)
  }

  override def quote(x: Exp[Any]) : String = x match {
    case Const(d: Date) => "new java.util.Date(" + d.getTime + "L)"
    case _ => super.quote(x)
  }
}