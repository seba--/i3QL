package sae.typecheck

import org.scalatest.FunSuite
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestExpDiff extends FunSuite {

  val t20 = makeBinAddTree(20, () => Num(1))
  test ("insert maximally shared tree with height 20") {
    t20.insert
  }

  test ("remove maximally shared tree with height 20") {
    t20.remove
  }

  test ("replace empty tree with maximally shared tree with height 20") {
    val e = Num(1)
    e.insert
    e.replaceWith(t20)
  }

//  val t21 = makeBinAddTree(21, () => Num(1))
//  test ("replace maximally shared tree with height 20 by maximally shared tree with height 21") {
//    t20.replaceWith(t21)
//  }

}
