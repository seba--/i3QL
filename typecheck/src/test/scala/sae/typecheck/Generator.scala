package sae.typecheck

/**
 * Created by seba on 05/11/14.
 */
object Generator {
  def makeBinAddTree(height: Int, leaveMaker: () => Exp): Exp = {
    if (height == 0)
      leaveMaker()
    else {
      val l = makeBinAddTree(height - 1, leaveMaker)
      val r = makeBinAddTree(height - 1, leaveMaker)
      if (l == r)
        Add(l, l)
      else
        Add(l, r)
    }
  }
}
