package idb.algebra.compiler.util

import idb.lms.extensions.ScalaCodegenExt

import scala.virtualization.lms.common.BaseExp

/**
  * Created by Mirko on 09.09.2016.
  */
trait FunctionBoxFactory extends ScalaCodegenExt {

	def create[A, B](f : IR.Rep[A => B]) : A => B = {
		//new FunctionBox(f)
		compileFunctionWithDynamicManifests(f)

//		val s2 = functionToScalaCodeWithDynamicManifests(f)
//
//		val List(mA, mB) = f.tp.typeArguments
//		val mAUnsafe =  mA.asInstanceOf[Manifest[A]]
//		val mBUnsafe = mB.asInstanceOf[Manifest[B]]
//		new FunctionBox2[A, B](s2._1, s2._2)(mAUnsafe, mBUnsafe)
	}


	case class FunctionBox[A, B](f : IR.Rep[A => B]) extends (A => B) with Serializable {

		@transient var compiledF : A => B = null

		def apply(x : A) : B = {
			if (compiledF == null)
				compiledF = compileFunctionWithDynamicManifests(f)

			compiledF(x)
		}
	}

	case class FunctionBox2[A : Manifest, B : Manifest](className : String, source : String) extends (A => B){

		var f : A => B = null

		def apply(x : A) : B = {
			if (f == null)
				f = compileScalaCode[A, B](className, source)
			f(x)
		}
	}


}
