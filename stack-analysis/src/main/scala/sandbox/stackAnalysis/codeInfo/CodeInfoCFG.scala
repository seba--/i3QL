package sandbox.stackAnalysis.codeInfo

import sae.bytecode.structure.CodeInfo
import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.ControlFlowAnalysis


/**
 * This class implements a control flow graph based on a Relation[CodeInfo].
 * This graph is used for forward analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 24.10.12
 * Time: 14:33
 */

//TODO: Instructions: jsr, athrow
object CodeInfoCFG extends ControlFlowAnalysis {
  /**
   * This method computes the preceding program counters based on an Array[Instruction]
   * @param ci The underlying code info of the control flow graph.
   * @return The array of transformers of a method. The indexes of the array are the valid program
   *         counters of the program. The transformers on non-PC indexes is null.
   */
  def computePredecessors(ci: CodeInfo): Array[List[Int]] = {

    val a = ci.code.instructions
    val res = Array.ofDim[List[Int]](a.length)


    var currentPC = 0
    var nextPC = 0

    while (nextPC < a.length && nextPC >= 0) {

      nextPC = a(currentPC).indexOfNextInstruction(currentPC, ci.code)



      if (nextPC < a.length && nextPC >= 0) {
        if (a(currentPC).isInstanceOf[ConditionalBranchInstruction]) {
          addToArray(res, nextPC, currentPC)
          addToArray(res, currentPC + a(currentPC).asInstanceOf[ConditionalBranchInstruction].branchoffset, currentPC)
        } else if (a(currentPC).isInstanceOf[UnconditionalBranchInstruction]) {
          addToArray(res, currentPC + a(currentPC).asInstanceOf[UnconditionalBranchInstruction].branchoffset, currentPC)
        } else if (a(currentPC).isInstanceOf[LOOKUPSWITCH]) {
          val instr = a(currentPC).asInstanceOf[LOOKUPSWITCH]
          for (p <- instr.npairs) {
            addToArray(res, p._2, currentPC)
          }
        } else if (a(currentPC).isInstanceOf[TABLESWITCH]) {
          val instr = a(currentPC).asInstanceOf[TABLESWITCH]
          for (p <- instr.jumpOffsets) {
            addToArray(res, p, currentPC)
          }
        } else if (a(currentPC).isInstanceOf[ReturnInstruction]) {
          //There is no control flow from a return instruction.
        } else if (a(currentPC).isInstanceOf[ATHROW.type]) {
          //TODO: fill in what exceptions do
        } else if (a(currentPC).isInstanceOf[JSR]) {
          addToArray(res, currentPC + a(currentPC).asInstanceOf[JSR].branchoffset, currentPC)

        } else if (a(currentPC).isInstanceOf[JSR_W]) {
          addToArray(res, currentPC + a(currentPC).asInstanceOf[JSR_W].branchoffset, currentPC)
        } else {
          addToArray(res, nextPC, currentPC)
        }
      }
      if (res(currentPC) == null) res(currentPC) = Nil
      currentPC = nextPC
    }

    return res

  }

  /**
   * This method add an element to a list in an array at a specified index.
   * @param a The array where the lists are stored.
   * @param index The index in the array.
   * @param add The element that should be added to a list.
   */
  private def addToArray(a: Array[List[Int]], index: Int, add: Int) {
    if (a(index) == null)
      a(index) = Nil
    a(index) = add :: a(index)
  }


}
