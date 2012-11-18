package sandbox.stackAnalysis

import de.tud.cs.st.bat.resolved._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 16:45
 */
object Types {

  object VarType extends Enumeration {
    val vAny, vBoolean, vByte, vChar, vShort, vInt, vFloat, vReference, vReturnAddress, vLong, vDouble, vNothing = Value
   }

  type StackResult = Result[Type, Int]
}
