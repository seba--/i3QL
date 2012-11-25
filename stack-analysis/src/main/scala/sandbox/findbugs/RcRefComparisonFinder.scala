package sandbox.findbugs

import sandbox.stackAnalysis.Result
import de.tud.cs.st.bat.resolved._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 23.11.12
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
class RcRefComparisonFinder extends BugFinder[Result[Type,Int]] {

  //TODO: implement
  def listensTo(): List[Instruction] = Nil

  //TODO: implement
  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[Result[Type,Int]], logger: BugLogger) = {

  }

}
