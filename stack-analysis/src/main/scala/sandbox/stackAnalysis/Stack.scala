package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable


/**
 * This class implements the stack that is used as an analysis result.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
case class Stack[T](maxSize: Int, stack: List[List[T]]) extends Combinable[Stack[T]] {
  require(stack.size <= maxSize && maxSize >= 0)

  def this(size: Int) = this(size, Nil)

  def push(t: T): Stack[T] =
    if (stack.size < maxSize)
      new Stack[T](maxSize, (t :: Nil) :: stack)
    else
      this

  def topElement(): List[T] =
    stack.head

  def pop(): Stack[T] =
    if(stack != Nil)
      new Stack[T](maxSize, stack.tail)
    else
      this

  def combineWith(other: Stack[T]): Stack[T] = {
    if (other == null)
      this
    else
      new Stack[T](if (maxSize > other.maxSize) maxSize else other.maxSize, combineWithList(stack, other.stack))

  }

  private def combineWithList(a: List[List[T]], b: List[List[T]]): List[List[T]] = {
    if (a == Nil)
      b
    else if (b == Nil)
      a
    else
      (a.head ++ b.head) :: combineWithList(a.tail, b.tail)
  }

  override def toString = "[" + stack.mkString(", ") + "]"


}
