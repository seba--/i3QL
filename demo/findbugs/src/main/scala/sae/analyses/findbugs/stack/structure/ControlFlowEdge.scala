package sae.analyses.findbugs.stack.structure

import sae.bytecode.instructions.InstructionInfo


/**
 *
 * @author Mirko
 * @author Ralf Mitschke
 */
case class ControlFlowEdge(current: InstructionInfo, next: InstructionInfo)
