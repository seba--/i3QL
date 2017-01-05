package idb.lms.extensions.operations

import scala.virtualization.lms.common._
import scala.reflect.SourceContext
import scala.language.implicitConversions


/**
 * @author Mirko Köhler
 */
trait EitherOps
	extends Base
	with Variables
{

	object Left {
		def apply[A : Manifest, B : Manifest] (x: Rep[A])(implicit pos: SourceContext) = left_new[A,B] (x)
	}

	object Right {
		def apply[A : Manifest, B: Manifest] (x: Rep[B])(implicit pos: SourceContext) = right_new[A,B] (x)
	}


	implicit def varToEitherOps[A: Manifest, B : Manifest] (x: Var[Either[A, B]]) = new EitherOpsCls (readVar (x))

	implicit def repEitherToEitherOps[A: Manifest, B : Manifest] (a: Rep[Either[A, B]]) = new EitherOpsCls (a)

	implicit def eitherToEitherOps[A: Manifest, B : Manifest] (a: Either[A, B]) = new EitherOpsCls (unit (a))

	class EitherOpsCls[A : Manifest, B : Manifest] (a: Rep[Either[A, B]])
	{
		def isRight (implicit pos: SourceContext) = either_isRight(a)

		def isLeft (implicit pos: SourceContext) = either_isLeft(a)

		def rightGet (implicit pos: SourceContext) = either_getRight(a)

		def leftGet (implicit pos: SourceContext) = either_getLeft(a)
	}

	def left_new[A : Manifest, B : Manifest] (x: Rep[A])(implicit pos: SourceContext): Rep[Either[A, B]]

	def right_new[A : Manifest, B : Manifest] (x : Rep[B])(implicit pos: SourceContext): Rep[Either[A, B]]

	def either_isRight[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[Boolean]

	def either_isLeft[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[Boolean]

	def either_getRight[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[B]

	def either_getLeft[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[A]

}

trait EitherOpsExp extends EitherOps with EffectExp {

	case class LeftNew[A: Manifest, B : Manifest] (x: Rep[A]) extends Def[Either[A, B]]

	case class RightNew[A : Manifest, B : Manifest] (x: Rep[B]) extends Def[Either[A, B]]

	case class EitherIsRight[A : Manifest, B : Manifest] (x: Exp[Either[A, B]]) extends Def[Boolean]

	case class EitherIsLeft[A : Manifest, B : Manifest] (x: Exp[Either[A, B]]) extends Def[Boolean]

	case class EitherGetRight[A : Manifest, B : Manifest] (x: Exp[Either[A, B]]) extends Def[B]

	case class EitherGetLeft[A : Manifest, B : Manifest] (x: Exp[Either[A, B]]) extends Def[A]


	override def left_new[A : Manifest, B : Manifest] (x: Rep[A])(implicit pos: SourceContext): Rep[Either[A, B]] =
		LeftNew[A,B](x)

	override def right_new[A : Manifest, B : Manifest] (x : Rep[B])(implicit pos: SourceContext): Rep[Either[A, B]] =
		RightNew[A,B](x)

	override def either_isRight[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[Boolean] =
		EitherIsRight(x)

	override def either_isLeft[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[Boolean] =
		EitherIsLeft(x)

	override def either_getRight[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[B] =
		EitherGetRight(x)

	override def either_getLeft[A : Manifest, B : Manifest] (x : Rep[Either[A, B]])(implicit pos: SourceContext): Rep[A] =
		EitherGetLeft(x)

	override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
		case LeftNew (x) => left_new (f (x))
		case RightNew (x) => right_new(f (x))
		case EitherIsRight (x) => either_isRight(f (x))
		case EitherIsLeft (x) => either_isLeft(f (x))
		case EitherGetRight (x) => either_getRight(f (x))
		case EitherGetLeft (x) => either_getLeft(f (x))
		case _ => super.mirror (e, f)
	}).asInstanceOf[Exp[A]]


}

trait BaseGenEitherOps extends ScalaGenBase
{
	val IR: EitherOpsExp


}

trait ScalaGenEitherOps extends BaseGenEitherOps with ScalaGenEffect
{

	val IR: EitherOpsExp

	import IR._

	override def emitNode (sym: Sym[Any], rhs: Def[Any]) = rhs match {
		case LeftNew (x) => emitValDef (sym, "Left(" + quote (x) + ")")
		case RightNew (x) => emitValDef (sym, "Right(" + quote (x) + ")")
		case EitherIsRight (x) => emitValDef (sym, "" + quote (x) + ".isRight")
		case EitherIsLeft (x) => emitValDef (sym, "" + quote (x) + ".isLeft")
		case EitherGetRight (x) => emitValDef (sym, "" + quote (x) + ".right.get")
		case EitherGetLeft (x) => emitValDef (sym, "" + quote (x) + ".left.get")
		case _ => super.emitNode (sym, rhs)
	}
}
