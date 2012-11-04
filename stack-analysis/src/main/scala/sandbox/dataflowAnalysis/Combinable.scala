package sandbox.dataflowAnalysis

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:01
 * To change this template use File | Settings | File Templates.
 */
trait Combinable[T] {
  def combineWith(other : T) : T
}
