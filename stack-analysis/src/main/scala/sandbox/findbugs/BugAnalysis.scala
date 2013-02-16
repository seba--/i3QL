package sandbox.findbugs

import detect._
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sandbox.stackAnalysis.instructionInfo.{StateInfo, IIStackAnalysis}
import sandbox.stackAnalysis.codeInfo.{MethodResultToStateInfoView, CIStackAnalysis}
import sandbox.stackAnalysis.instructionInfo.StateInfo
import sae.syntax.RelationalAlgebraSyntax._
import sandbox.stackAnalysis.StackAnalysis

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 15.02.13
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
case object BugAnalysis extends (BytecodeDatabase => Relation[BugInfo]) {

   def apply(bcd : BytecodeDatabase) : Relation[BugInfo] = {
     computeBugInfos(StackAnalysis(bcd))
   }

  def byInstructionInfo(bcd : BytecodeDatabase) : Relation[BugInfo] = {
     computeBugInfos(StackAnalysis.byInstructionInfo(bcd))
  }

  def byCodeInfo(bcd : BytecodeDatabase) : Relation[BugInfo] = {
    computeBugInfos(StackAnalysis.byCodeInfo(bcd))
  }


  private def computeBugInfos(stateInfos : Relation[StateInfo]) : Relation[BugInfo] = {
    return DL_SYNCHRONIZATION(stateInfos) ⊎
      DMI_INVOKING_TOSTRING_ON_ARRAY(stateInfos) ⊎
      RC_REF_COMPARISON(stateInfos) ⊎
      RV_RETURN_VALUE_IGNORED(stateInfos) ⊎
      SA_FIELD_SELF_COMPARISON(stateInfos) ⊎
      SA_LOCAL_SELF_ASSIGNMENT(stateInfos) ⊎
      SQL_BAD_PREPARED_STATEMENT_ACCESS(stateInfos)
  }




}
