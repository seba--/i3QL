package sandbox.ast

import scala.virtualization.lms.common.ScalaOpsPkgExp


/**
 * @author Ralf Mitschke
 */

//with ScalaOpsPkg
// with ScalaOpsPkg with ScalaOpsPkgExp
// LiftAll with BaseExp with NumericOpsExp with VariablesExp // works
object Main
{

  //val IR: ScalaOpsPkgExp = Arithmetics

  //import Arithmetics._

  def main (args: Array[String]) {
    //println(valueToAst(Arithmetics.power(2) _))
    //println(fun1ToAst(Arithmetics.power(2) _))
    //println(valueToAst(1 + 2))

    //println(fun2ToAst(Arithmetics.power))
    //printLambda(Arithmetics.shiftLeft)  // does not compile


    //printLambda(shiftLeftExp)

    /*
    val f: Sym[Int] = shiftLeft (IR.unit (32)).asInstanceOf[Sym[Int]]
    println (findDefinition (f))
    */

    val f: Arithmetics.Exp[Int] => Arithmetics.Exp[Int] = Arithmetics.shiftLeft



    val e = Arithmetics.reifyEffects {
      f
    }

    println(e)

    /*
    val f1: Sym[Int] =
      reifyEffects {
        f
      }.res.asInstanceOf[Sym[Int]]

    println (
      findDefinition (
        f1
      )
    )
  */
  }

  /*
  def valueToAst[A](test: Exp[A]): Exp[A] =
    test

  def fun1ToAst[A, R](test: Exp[A => R]): Exp[A => R] = {
    test
  }


  def fun2ToAst[A, B, R](test: Exp[(A, B) => R]): Exp[(A, B) => R] = {
    test
  }


  def printLambda[A: Manifest, B: Manifest](fun: Rep[A] => Rep[B])(implicit pos: SourceContext) {
    println(fun)
    println(pos)
  }


  def shiftLeftExp(base: Rep[Int]): Rep[Int] = {
    base / 2
  }
  */

}
