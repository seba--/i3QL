package sandbox.findbugs

import de.tud.cs.st.bat.resolved.Instruction

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
trait BugFinder[T] {

  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[T], logger: BugLogger)
}
