package sandbox.dataflowAnalysis

import sae.{Relation, QueryResult}
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
    compile(SELECT((g: CFGEntry, t: TransformerEntry[T]) => JoinEntry(g.methodDeclaration, g.predecessorArray, t.generators)) FROM(graph.result, transformers.result) WHERE (((_: CFGEntry).methodDeclaration) === ((_: TransformerEntry[T]).methodDeclaration)))

  val result: QueryResult[ResultEntry[T]] =
    compile(SELECT((ci: CodeInfo, je: JoinEntry) => ResultEntry[T](ci.declaringMethod, computeResult(ci, je.cfg, je.transformers))) FROM(codeInfo, res1) WHERE (((_: CodeInfo).declaringMethod) === ((_: JoinEntry).methodDeclaration)))

  def startValue(ci: CodeInfo): T

  def emptyValue(ci: CodeInfo): T

  private def computeResult(ci: CodeInfo, cfg: Array[List[Int]], transformers: Array[T => T]): Array[T] = {

    val sv = startValue(ci)
    val results: Array[T] = Array.fill[T](cfg.length)(emptyValue(ci))
    var resultsChanged = true
    var loops = 0

    // while (loop || oldResults != newResults) {
    while (resultsChanged) {
      resultsChanged = false


      var pc: Int = 0
      while (pc < cfg.length && pc >= 0) {

        if (transformers(pc) != null && cfg(pc) != null) {
          val preds: List[Int] = cfg(pc)
          /*    if (preds.length == 1) {
           val transformers = transformers(pc)(results(preds(0)))

           if (!transformers.equals(results(pc))) {
           // println("Res1==: " + transformers + " != " + results(pc))
             resultsChanged = true
           }

           results(pc) = transformers
         } else*/
          if (preds.length == 0) {
            /*Code for entry results of labels*/
            val result = sv
            /*Code for exit results of labels*/
            //val transformers = transformers(pc)(sv)

            if (!result.equals(results(pc))) {
              //  println("Res2==: " + transformers + " != " + results(pc))
              resultsChanged = true
            }

            results(pc) = result
          } else {

            /*Code for entry results of labels*/
            var result: T = transformers(preds.head)(results(preds.head))
            for (i <- 1 until preds.length) {
              result = (transformers(preds(i))(results(preds(i)))).combineWith(result)
            }
            /*End*/

            /*Code for exit results of labels
         var transformers: T = results(preds.head)
          for (i <- 1 until preds.length) {
            transformers = results(preds(i)).combineWith(transformers)
          }
         transformers = transformers(pc)(transformers) */
            /*End*/

            if (!result.equals(results(pc))) {
              // println("Res3==: " + transformers + " != " + results(pc))
              resultsChanged = true
            }

            results(pc) = result

          }
        }

        pc = CodeInfoTools.getNextPC(ci.code.instructions, pc)
      }
      loops = loops + 1
    }
    println("Loops: " + loops)
    //println(results.mkString("Resultarray:", "\n", "."))

    for (i <- 0 until results.length) {
      if (ci.code.instructions(i) != null) {
        print(ci.code.instructions(i).mnemonic + ": ")
        println(results(i))
      }
    }

    return results

  }


}
