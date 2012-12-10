package sandbox.dataflowAnalysis

import de.tud.cs.st.bat.resolved.Instruction

/**
 * Defines the transformers transformers of a data flow analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
trait ResultTransformer[T] {
  /**
   * The type of transformers that are used in the analysis.
   */
  type Transformer = (T => T)

  def getTransformer(pc: Int, instr: Instruction): Transformer


}
