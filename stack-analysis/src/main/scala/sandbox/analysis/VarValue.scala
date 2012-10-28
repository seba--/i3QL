package sandbox.analysis

/**
 * Values for the local variables of the stack analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 16:45
 * To change this template use File | Settings | File Templates.
 */
object VarValue extends Enumeration {
  val vNothing, vInt, vFloat, vObject, vLong, vDouble = Value
}
