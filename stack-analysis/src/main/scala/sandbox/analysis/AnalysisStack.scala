package sandbox.analysis

import collection.immutable.Queue

/**
 * This class implements the stack that is used as an analysis result.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
class AnalysisStack[T: Manifest](q: Queue[T]) {
  def this() = this(Queue.empty[T])

  private val stack: Queue[T] = q

  def push(t: T): AnalysisStack[T] =
    new AnalysisStack[T](stack.enqueue(t))

  def topElement(): T =
    stack.head

  def pop(): AnalysisStack[T] =
    new AnalysisStack[T](stack.dequeue._2)

  override def toString = "[" + stack.mkString(", ") + "]"


}
