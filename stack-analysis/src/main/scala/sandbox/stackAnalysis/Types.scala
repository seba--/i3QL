package sandbox.stackAnalysis


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 16:45
 */
object Types {

  object VarType extends Enumeration {
    val vBoolean, vByte, vChar, vShort, vInt, vFloat, vReference, vReturnAddress, vLong, vDouble = Value
  }

  type VarEntry = (Int, VarType.Value)
  type StackResult = Result[VarEntry, VarEntry]
}
