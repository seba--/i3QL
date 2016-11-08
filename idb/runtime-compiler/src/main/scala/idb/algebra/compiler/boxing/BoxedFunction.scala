package idb.algebra.compiler.boxing

import idb.lms.extensions.ScalaCodegenExt

/**
  * Created by Mirko on 13.09.2016.
  */
case class BoxedFunction[A, B](code : ClassCode[A,B]) extends (A => B) {

	var f : A => B = null

	def compile(compiler : ScalaCodegenExt): Unit = {
		f = compiler.compileScalaCode[A,B](code)
	}

	def apply(x : A) : B = {
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
