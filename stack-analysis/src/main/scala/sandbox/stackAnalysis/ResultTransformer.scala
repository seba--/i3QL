package sandbox.stackAnalysis

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
trait ResultTransformer[T] extends ((Int, Instruction, T) => T) {

}
