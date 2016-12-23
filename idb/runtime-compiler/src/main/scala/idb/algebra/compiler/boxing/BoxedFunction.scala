package idb.algebra.compiler.boxing

import idb.lms.extensions.ScalaCodegenExt

case class BoxedFunction[A, B](code : ClassCode[A,B]) extends (A => B) {

	var f : A => B = null

	def compile(compiler : ScalaCodegenExt): Unit = {
		Predef.println("BoxedFunction.compile >>>")
		Predef.println(code)
		Predef.println("<<<")
		f = compiler.compileScalaCode[A,B](code)
	}

	def apply(x : A) : B = {
		if (f == null) {
			Predef.println("[BoxedFunction] Warning! Function is not defined, x=" + x)
			return null.asInstanceOf[B]
		}

		f.apply(x)
	}

	override def toString : String =
		s"BoxedFunction[compiled=${f != null}]($f)"
}

object BoxedFunction {
	def compile[A, B](f : A => B, compiler : ScalaCodegenExt) : A => B = f match {
		case b@BoxedFunction(_) =>
			b.compile(compiler)
			f
		case _ =>
			f
	}
}
