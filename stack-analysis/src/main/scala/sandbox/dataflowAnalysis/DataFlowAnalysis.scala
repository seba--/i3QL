package sandbox.dataflowAnalysis

import sae.Relation
import sae.syntax.sql._
import sae.bytecode.structure.CodeInfo
import de.tud.cs.st.bat.resolved.Instruction
import sae.bytecode.BytecodeDatabase


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
abstract class DataFlowAnalysis[T <: Combinable[T]](vGraph: ControlFlowAnalysis, vTransformers: ResultTransformer[T])(implicit m: Manifest[T]) extends (BytecodeDatabase => Relation[MethodResult[T]]) {

  val controlFlowAnalysis: ControlFlowAnalysis = vGraph
  val transformers: ResultTransformer[T] = vTransformers

  /**
   * Set to true, if the results should be printed during the dataflow analysis.
   */
  var printResults = false

  def apply(bcd: BytecodeDatabase): Relation[MethodResult[T]] = {
    val cfg: Relation[MethodCFG] = controlFlowAnalysis(bcd)

    compile(SELECT((ci: CodeInfo, cfg: MethodCFG) => MethodResult[T](ci.declaringMethod, computeResult(ci, cfg.predecessorArray))) FROM(bcd.code, cfg) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodCFG).declaringMethod)))
  }

  def startValue(ci: CodeInfo): T

  def emptyValue(ci: CodeInfo): T

  private def computeResult(ci: CodeInfo, cfg: Array[List[Int]]): Array[T] = {

    //The start value of the analysis.
    val sv = startValue(ci)
    val ev = emptyValue(ci)

    //Initialize the result array with the empty value.
    val results: Array[T] = Array.ofDim[T](cfg.length)
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
        if (preds.length != 0) {

          //Result = transform the results at the entry labels with their transformer then combine them for a new result.
          result = transform(preds.head, ci.code.instructions, fromArray(results, preds.head, ev))
          for (i <- 1 until preds.length) {
            result = (transform(preds(i), ci.code.instructions, fromArray(results, preds(i), ev))).combineWith(result)
          }
        }

        //Check if the result has changed. If no result was changed during one iteration, the fixed point has been found.
        if (!result.equals(results(pc))) {
          resultsChanged = true
        }

        //Set the new result in the result array.
        results(pc) = result
        //Set the next program counter.
        pc = ci.code.instructions(pc).indexOfNextInstruction(pc, ci.code)

      }


    }
    //Print out results.
    if (printResults) {
      println(ci.declaringMethod)
      println(cfg.mkString("CFG: ", ", ", ""))
      for (i <- 0 until results.length) {
        if (ci.code.instructions(i) != null) {
          println("\t" + results(i))
          println(i + "\t" + ci.code.instructions(i))

        }
      }
      println()
    }
    //  println()
    return results

  }

  private def transform(fromPC: Int, a: Array[Instruction], currentResult: T): T = {
    transformers.getTransformer(fromPC, a(fromPC))(currentResult)
  }

  private def fromArray(ts: Array[T], index: Int, default: T) = {
    if (ts(index) == null)
      default
    else
      ts(index)
  }


}
