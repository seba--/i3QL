package sandbox.findbugs

import de.tud.cs.st.bat.resolved._
import sandbox.stackAnalysis._
import de.tud.cs.st.bat.resolved.ISTORE
import de.tud.cs.st.bat.resolved.FSTORE
import de.tud.cs.st.bat.resolved.DSTORE
import de.tud.cs.st.bat.resolved.LSTORE
import de.tud.cs.st.bat.resolved.ISTORE
import sandbox.stackAnalysis.Configuration
import de.tud.cs.st.bat.resolved.ASTORE
import de.tud.cs.st.bat.resolved.LSTORE
import de.tud.cs.st.bat.resolved.FSTORE
import de.tud.cs.st.bat.resolved.DSTORE

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 02.12.12
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
class LocalSelfAssignmentFinder extends BugFinder[Configuration] {

  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[Configuration], logger: BugLogger) = {
    val instr = instructions(pc)

    if(instr.isInstanceOf[StoreLocalVariableInstruction]) {
      checkStoreInstruction(pc,instructions,analysis,logger)
    }
  }

  private def checkStoreInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[Configuration], logger: BugLogger) = {
    val lvIndex = getIndexOfLocalVariable(instructions(pc).asInstanceOf[StoreLocalVariableInstruction])

    val originatesFromSelf = analysis(pc).l(lvIndex).possibilities.forall(t => originatesFromLocVar(t,lvIndex,instructions,analysis))

    if(originatesFromSelf)
      logger.log(pc,BugType.SA_LOCAL_SELF_ASSIGNMENT)

  }

  private def originatesFromLocVar(to : TypeOption, lvIndex : Int, instructions: Array[Instruction], analysis: Array[Configuration]) : Boolean = {
     false
  }

  private def getIndexOfLocalVariable(instr : StoreLocalVariableInstruction) : Int = {
    instr match {
      case ISTORE(x) => x

      case LSTORE(x) => x

      case FSTORE(x) => x

      case DSTORE(x) => x

      case ASTORE(x) => x

      case ISTORE_0 | LSTORE_0  | FSTORE_0 | DSTORE_0 | ASTORE_0 => 0
      case ISTORE_1 | LSTORE_1  | FSTORE_1 | DSTORE_1 | ASTORE_1 => 1
      case ISTORE_2 | LSTORE_2  | FSTORE_2 | DSTORE_2 | ASTORE_2 => 2
      case ISTORE_3 | LSTORE_3  | FSTORE_3 | DSTORE_3 | ASTORE_3 => 3

      case x => throw new IllegalArgumentException(x + ": The store instruction is unknown.")
    }
  }



}
