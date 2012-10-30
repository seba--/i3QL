package sandbox.analysis

import collection.immutable.{HashSet, Queue}

/**
 * This class implements the stack that is used as an analysis result.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
// TODO use lists
class AnalysisStack[T: Manifest](size: Int, q: Queue[Set[T]]) {
  require(q.size <= size && size >= 0)

  def this(size: Int) = this(size, Queue.empty[Set[T]])

  private val maxSize = size
  private val stack: Queue[Set[T]] = q

  def push(t: T): AnalysisStack[T] =
    if (stack.size < maxSize)
      new AnalysisStack[T](maxSize, stack.enqueue(new HashSet[T] + t))
    else
      this

  def topElement(): Set[T] =
    stack.head

  def pop(): AnalysisStack[T] =
    new AnalysisStack[T](maxSize, stack.dequeue._2)

  override def toString = "[" + stack.mkString(", ") + "]"


}
