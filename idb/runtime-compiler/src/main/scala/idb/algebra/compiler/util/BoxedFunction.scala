package idb.algebra.compiler.util

import idb.lms.extensions.ScalaCodegenExt

/**
  * Created by Mirko on 13.09.2016.
  */
case class BoxedFunction[A, B](code : ClassCode[A,B]) extends (A => B) {

	@transient var f : A => B = null

	def compile(compiler : ScalaCodegenExt): Unit = {
		f = compiler.compileScalaCode[A,B](code)
	}

	def apply(x : A) : B = {
		f.apply(x)
	}
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
