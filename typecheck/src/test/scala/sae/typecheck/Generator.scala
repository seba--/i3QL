package sae.typecheck

import sae.typecheck.Exp.ExpKind

import scala.collection.mutable.ArrayBuffer

/**
 * Created by seba on 05/11/14.
 */
object Generator {
  def makeBinAddTree(height: Int, leaveMaker: () => Exp) = makeBinTree(height, Add, leaveMaker)
  def makeBinAppTree(height: Int, leaveMaker: () => Exp) = makeBinTree(height, App, leaveMaker)

  def makeBinTree(height: Int, kind: ExpKind, leaveMaker: () => Exp): Exp = {
    val leaveCount = Math.pow(2, height-1).toInt
    val ts = Array.ofDim[Exp](leaveCount)

    for (i <- 0 until leaveCount)
      ts(i) = leaveMaker()

    for (h <- height to 1 by -1)
      for (i <- 0 until Math.pow(2, h-1).toInt-1 by 2) {
        val l = ts(i)
        val r = ts(i+1)
        if (l == r)
          ts(i/2) = kind(l, l)
        else
          ts(i/2) = kind(l, r)
      }

    ts(0)
  }

  def makeFunType(length: Int, returnType: Type, argMaker: () => Type): Type = {
    var argTypes = Seq[Type]()
    for (i <- 1 to length)
      argTypes = argMaker() +: argTypes
    var t = returnType
    for (i <- 1 to length) {
      t = TFun(argTypes.head, t)
      argTypes = argTypes.tail
    }
    t
  }
}
