package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.structure.MethodDeclaration
import de.tud.cs.st.bat.resolved.Instruction
import sandbox.stackAnalysis.datastructure.State

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 15.02.13
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */
case class StateInfo(declaringMethod : MethodDeclaration, pc : Int, instruction : Instruction, state : State) {

}
