package sae.typecheck

import org.scalatest.FunSuite
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestExpDiff extends FunSuite {

  val t10 = makeBinAddTree(10, () => Num(1))
  val t15 = makeBinAddTree(15, () => Num(1))
  val t20 = makeBinAddTree(20, () => Num(1))

  test ("insert tree(h=20)") {
    t20.insert
  }

  test ("remove tree(h=20)") {
    t20.remove
  }

  test ("replace tree(h=0) with tree(h=10)") {
    val e = Num(1)
    e.insert
    e.replaceWith(t10)
  }

  test ("replace tree(h=10) with tree(h=15)") {
    t10.replaceWith(t15)
  }

  test ("replace tree(h=15) with tree(h=20)") {
    t15.replaceWith(t20)
  }

  test ("replace tree(h=20) with tree(h=15)") {
    t20.replaceWith(t15)
  }

  test ("replace tree(h=15) with tree(h=10)") {
    t15.replaceWith(t10)
  }

  test ("replace tree(h=10) with tree(h=0)") {
    val e = Num(1)
    t10.replaceWith(e)
    e.remove
  }



  //  val t21 = makeBinAddTree(21, () => Num(1))
//  test ("replace maximally shared tree with height 20 by maximally shared tree with height 21") {
//    t20.replaceWith(t21)
//  }

}
