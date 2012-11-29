package sandbox.dataflowAnalysis

import sae.Relation
import sae.syntax.sql._
import sae.bytecode.structure.{CodeInfo, MethodDeclaration}
import sandbox.stackAnalysis.CodeInfoTools

/**
 * Abstract class for dataflow analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
// rename to control flow analysis
abstract case class DataFlowAnalysis[T <: Combinable[T]](codeInfo: Relation[CodeInfo], graph: AnalysisCFG, transformers: ResultTransformer[T])(implicit m: Manifest[T]) {

  private case class JoinEntry(methodDeclaration: MethodDeclaration, cfg: Array[List[Int]], transformers: Array[T => T]) {

  }

  private val res1: Relation[JoinEntry] =
    compile(SELECT((g: MethodCFG, t: MethodTransformer[T]) => JoinEntry(g.methodDeclaration, g.predecessorArray, t.generators)) FROM(graph.result, transformers.result) WHERE (((_: MethodCFG).methodDeclaration) === ((_: MethodTransformer[T]).declaringMethod)))

  val result: Relation[MethodResult[T]] =
    compile(SELECT((ci: CodeInfo, je: JoinEntry) => MethodResult[T](ci.declaringMethod, computeResult(ci, je.cfg, je.transformers))) FROM(codeInfo, res1) WHERE (((_: CodeInfo).declaringMethod) === ((_: JoinEntry).methodDeclaration)))

  def startValue(ci: CodeInfo): T

  def emptyValue(ci: CodeInfo): T

  private def computeResult(ci: CodeInfo, cfg: Array[List[Int]], transformers: Array[T => T]): Array[T] = {
    //The start value of the analysis.
    val sv = startValue(ci)
    //The empty value of the analysis
    val ev = emptyValue(ci)

    //Initialize the result array with the empty value.
    val results: Array[T] = Array.fill[T](cfg.length)(ev)
    //Indicator for the fixed point.
    var resultsChanged = true

    //Iterates until fixed point is reached.
    while (resultsChanged) {
      resultsChanged = false

      var pc: Int = 0
      //Iterates over all program counters.
      while (pc < cfg.length && pc >= 0) {

        //The predecessors for the instruction at program counter pc.
        val preds: List[Int] = cfg(pc)
        //Result for this iteration for the instruction at program counter pc.
        var result: T = sv

        //Initializes the results array.

        //If the instruction has no predecessors, the result will be the start value (sv)
        //TODO: Null check should be obosolete when exceptions are implemented
        if (preds != null && preds.length != 0) {
          //Result = transform the results at the entry labels with their transformer then combine them for a new result.
          result = transformers(preds.head)(results(preds.head))
          for (i <- 1 until preds.length) {
            result = (transformers(preds(i))(results(preds(i)))).combineWith(result)
          }
        }

        //Check if the result has changed. If no result was changed during one iteration, the fixed point has been found.
        if (!result.equals(results(pc))) {
          resultsChanged = true
        }
        //Set the new result in the result array.
        results(pc) = result
        //Set the next program counter.
        /*     val savedPC = pc

    pc = ci.code.instructions(pc).indexOfNextInstruction(pc,ci.code)
    if(pc < ci.code.instructions.length && ci.code.instructions(pc) == null) {
      pc = CodeInfoTools.getNextPC(ci.code.instructions,savedPC)
    }    */
        pc = CodeInfoTools.getNextPC(ci.code.instructions, pc)
      }


    }
    //Print out results.


    for (i <- 0 until results.length) {
      if (ci.code.instructions(i) != null) {
        println("\t" + results(i))
        println(i + "\t" + ci.code.instructions(i).mnemonic)

      }
    }
    //  println()
    return results

  }


}
