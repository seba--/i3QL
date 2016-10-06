package idb.lms.extensions.operations

import scala.reflect.SourceContext
import scala.virtualization.lms.common.{ScalaGenSetOps, SetOpsExp}
import scala.language.implicitConversions

/**
 * @author Mirko Köhler
 */
trait SetOpsExpExt extends SetOpsExp {

	implicit def varToSetOps[A:Manifest](x: Var[Set[A]]) = new SetOpsClsExt(readVar(x))
	implicit def repSetToSetOps[T:Manifest](a: Rep[Set[T]]) = new SetOpsClsExt(a)
	implicit def setToSetOps[T:Manifest](a: Set[T]) = new SetOpsClsExt(unit(a))

	class SetOpsClsExt[T:Manifest](a: Rep[Set[T]])  {
		def ++(b: Rep[Set[T]])(implicit pos: SourceContext) = set_append(a,b)
	}

	case class SetAppend[T:Manifest](xs: Exp[Set[T]], ys: Exp[Set[T]]) extends Def[Set[T]]


	def set_append[T:Manifest](xs: Exp[Set[T]], ys: Exp[Set[T]])(implicit pos: SourceContext): Exp[Set[T]] = SetAppend(xs,ys)


	override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
		case SetAppend (xs,ys) => set_append (f (xs), f(ys)) //TODO Is this correct?
		case _ => super.mirror (e, f)
	}).asInstanceOf[Exp[A]]

}


trait ScalaGenSetOpsExt
	extends ScalaGenSetOps
{
	val IR: SetOpsExpExt
	import IR._

	override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
		case SetAppend(xs,ys) => emitValDef(sym, quote(xs) + " ++ " + quote(ys))
		case _ => super.emitNode(sym, rhs)
	}
}


