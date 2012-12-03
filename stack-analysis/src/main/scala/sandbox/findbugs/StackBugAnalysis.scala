package sandbox.findbugs

import sandbox.stackAnalysis.{CodeInfoTools, StackAnalysis, Configuration}
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sae.Relation
import sandbox.dataflowAnalysis.MethodResult
import sae.syntax.sql._


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
class StackBugAnalysis(ciRel: Relation[CodeInfo], analysis: StackAnalysis) {

  var printResults = false

  case class BugEntry(declaringMethod: MethodDeclaration, log: BugLogger) {

  }

  val result: Relation[BugEntry] =
    compile(SELECT ((ci : CodeInfo, mr : MethodResult[Configuration]) => BugEntry(ci.declaringMethod, computeBugLogger(ci,mr))) FROM (ciRel, analysis.result) WHERE (((_ : CodeInfo).declaringMethod) === ((_ : MethodResult[Configuration]).declaringMethod)))

 private def computeBugLogger(ci : CodeInfo, mr : MethodResult[Configuration]) : BugLogger = {

   val logger : BugLogger = new BugLogger()
   val instructionArray = ci.code.instructions
   val analysisArray = mr.resultArray

   var currentPC = 0

   while (currentPC < instructionArray.length && currentPC != -1) {
     for(bugFinder <- StackBugAnalysis.BUGFINDER_LIST) {
        bugFinder.notifyInstruction(currentPC,instructionArray,analysisArray,logger)
     }

     //TODO: change when bug fixed
     val savedPC = currentPC
     currentPC = instructionArray(currentPC).indexOfNextInstruction(currentPC, ci.code)
     if (currentPC < instructionArray.length && instructionArray(currentPC) == null) {
       currentPC = CodeInfoTools.getNextPC(instructionArray, savedPC)
     }
     //TODO: change when bug fixed

   }
   if(printResults) {
     println("BugFinder: " + logger.getLog())
   }

   return logger
 }

}

object StackBugAnalysis {
  val BUGFINDER_LIST : List[BugFinder[Configuration]] = new RefComparisonFinder :: new LocalSelfAssignmentFinder :: Nil
}
