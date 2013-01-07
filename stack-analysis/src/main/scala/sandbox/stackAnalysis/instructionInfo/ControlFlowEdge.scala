package sandbox.stackAnalysis.instructionInfo


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.12.12
 * Time: 15:12
 * To change this template use File | Settings | File Templates.
 */
case class ControlFlowEdge(previous: ControlFlowVertex, next: ControlFlowVertex) {

  /*override def toString() : String = {
    if(previous != null && next != null)
      return "<" + previous.instruction.pc + ">" + previous.instruction.instruction.mnemonic + " --> " + "<" + next.instruction.pc + ">" + next.instruction.instruction.mnemonic
    else if (next != null)
      return " --> " + "<" + next.instruction.pc + ">" + next.instruction.instruction.mnemonic
    else
      return "Nothing"
  }     */
}
