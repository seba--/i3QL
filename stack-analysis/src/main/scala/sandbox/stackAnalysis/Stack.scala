package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable


/**
 * This class implements the s that is used as an analysis transformers.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
case class Stack[T](maxSize: Int, stack: List[(Int, List[T])]) extends Combinable[Stack[T]] {
  require(stack.size <= maxSize && maxSize >= 0)

  def this(size: Int) = this(size, Nil)

  var size = getSize(stack)

  private def getSize(l: List[(Int, List[T])]): Int = {
    if (l == Nil)
      0
    else
      l.head._1 + getSize(l.tail)
  }

  def push(tSize: Int, t: T): Stack[T] =
    if (size + tSize <= maxSize)
      new Stack[T](maxSize, (tSize, (t :: Nil)) :: stack)
    else
      this

  def push(t: T): Stack[T] =
    push(1, t)

  def topElement(): List[T] =
    stack.head._2

  def pop(): Stack[T] =
    if (stack != Nil)
      new Stack[T](maxSize, stack.tail)
    else
      this

  def combineWith(other: Stack[T]): Stack[T] = {
    if (other == null)
      this
    else
      new Stack[T](if (maxSize > other.maxSize) maxSize else other.maxSize, combineWithList(stack, other.stack))

  }

  private def combineWithList(a: List[(Int, List[T])], b: List[(Int, List[T])]): List[(Int, List[T])] = {
    if (a == Nil)
      b
    else if (b == Nil)
      a
    else
    //TODO a.head._1 != b.head._1 (if the size of the stack fragments are not equal.
      (a.head._1, (CodeInfoTools.distinctAppend(a.head._2, b.head._2))) :: combineWithList(a.tail, b.tail)
  }

  override def toString = {
    var stringList: List[String] = Nil
    for (l <- stack)
      stringList = l._2.mkString(l._1 + "{", ",", "}") :: stringList
    stringList.reverse.mkString("S: (", "; ", ")")
  }


}
