package sae.analyses.findbugs

import stack.structure.StateInfo
import sae.bytecode.instructions.InstructionInfo

/**
 *
 * @author Ralf Mitschke
 *
 */
package object stack
{
    def instruction : StateInfo => InstructionInfo = _.instruction


}
