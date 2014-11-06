package sae.typecheck

import org.scalatest.FunSuite
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestExpDiff extends FunSuite {

  test ("insert maximally shared tree with height 20") {
    val e = makeBinAddTree(20, () => Num(1))
    e.insert
  }

}
