package sandbox.stackAnalysis.codeInfo

import sae.bytecode.structure.CodeInfo
import de.tud.cs.st.bat.resolved._


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
object CIControlFlowAnalysis extends ControlFlowAnalysis {
  /**
   * This method computes the preceding program counters based on an Array[Instruction]
   * @param ci The underlying code info of the control flow graph.
   * @return The array of transformers of a method. The indexes of the array are the valid program
   *         counters of the program. The transformers on non-PC indexes is null.
   */
  def computePredecessors(ci: CodeInfo): Array[List[Int]] = {

    val instructions = ci.code.instructions
    val cfg = Array.fill[List[Int]](instructions.length)(Nil)

    var currentPC = 0
    var nextPC = 0

    while (nextPC < instructions.length && nextPC >= 0) {

      nextPC = instructions(currentPC).indexOfNextInstruction(currentPC, ci.code)

      if (nextPC < instructions.length && nextPC >= 0) {
        if (instructions(currentPC).isInstanceOf[ConditionalBranchInstruction]) {
          add(cfg, nextPC, currentPC)
          add(cfg, currentPC + instructions(currentPC).asInstanceOf[ConditionalBranchInstruction].branchoffset, currentPC)
        } else if (instructions(currentPC).isInstanceOf[UnconditionalBranchInstruction]) {
          add(cfg, currentPC + instructions(currentPC).asInstanceOf[UnconditionalBranchInstruction].branchoffset, currentPC)
        } else if (instructions(currentPC).isInstanceOf[LOOKUPSWITCH]) {
          val instr = instructions(currentPC).asInstanceOf[LOOKUPSWITCH]
          for (p <- instr.npairs) {
            add(cfg, p._2, currentPC)
          }
        } else if (instructions(currentPC).isInstanceOf[TABLESWITCH]) {
          val instr = instructions(currentPC).asInstanceOf[TABLESWITCH]
          for (p <- instr.jumpOffsets) {
            add(cfg, p, currentPC)
          }
        } else if (instructions(currentPC).isInstanceOf[ReturnInstruction]) {
          //There is no control flow from a return instruction.
        } else if (instructions(currentPC).isInstanceOf[ATHROW.type]) {
          //TODO: fill in what exceptions do
        } else if (instructions(currentPC).isInstanceOf[JSR]) {
          add(cfg, currentPC + instructions(currentPC).asInstanceOf[JSR].branchoffset, currentPC)

        } else if (instructions(currentPC).isInstanceOf[JSR_W]) {
          add(cfg, currentPC + instructions(currentPC).asInstanceOf[JSR_W].branchoffset, currentPC)
        } else {
          add(cfg, nextPC, currentPC)
        }
      }

      currentPC = nextPC
    }

    return cfg

  }

  /**
   * This method add an element to a list in an array at a specified index.
   * @param a The array where the lists are stored.
   * @param index The index in the array.
   * @param add The element that should be added to a list.
   */
  private def add(a: Array[List[Int]], index: Int, add: Int) {
    if (a(index) == null)
      a(index) = Nil
    a(index) = add :: a(index)
  }


}
