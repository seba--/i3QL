package sandbox.stackAnalysis

import codeInfo.{CIStackAnalysis, MethodResultToStateInfoView}
import instructionInfo.{StateInfo, IIStackAnalysis}
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sandbox.findbugs.BugInfo
import codeInfo.CIStackAnalysis._


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.02.13
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
case object StackAnalysis extends (BytecodeDatabase => Relation[StateInfo]) {

  def apply(bcd : BytecodeDatabase) : Relation[StateInfo] = {
    byDefault(bcd)
  }

  private def defaultByCodeInfo = false

  private def byDefault(bcd : BytecodeDatabase) : Relation[StateInfo] =
    if(defaultByCodeInfo)
      byCodeInfo(bcd)
    else
      byInstructionInfo(bcd)

  def byInstructionInfo(bcd : BytecodeDatabase) : Relation[StateInfo] = {
    IIStackAnalysis(bcd)
  }

  def byCodeInfo(bcd : BytecodeDatabase) : Relation[StateInfo] = {
    new MethodResultToStateInfoView(bcd.code,CIStackAnalysis(bcd))
  }

  def byCodeInfoUnwrapped(bcd : BytecodeDatabase) : Relation[MethodStates] = {
    CIStackAnalysis(bcd)
  }
}
