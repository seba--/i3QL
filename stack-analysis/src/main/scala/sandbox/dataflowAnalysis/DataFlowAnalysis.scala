package sandbox.dataflowAnalysis

import sae.Relation
import sae.syntax.sql._
import sae.bytecode.structure.MethodDeclaration

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
abstract case class DataFlowAnalysis[T <: Combinable[T]](graph: AnalysisCFG, transformers: ResultTransformer[T])(implicit m: Manifest[T]) {

  val analysisResult: Relation[ResultEntry[T]] =
    compile(SELECT((g: CFGEntry, t: TransformerEntry[T]) => ResultEntry[T](g.methodDeclaration, computeResultPriv(startValue(g.methodDeclaration), g.predecessorArray, t.transformerArray))) FROM(graph.predecessors, transformers.functions) WHERE (((_: CFGEntry).methodDeclaration) === ((_: TransformerEntry[T]).methodDeclaration)))

  def startValue(m: MethodDeclaration): T

  private def computeResultPriv(sv: T, cfg: Array[List[Int]], transformer: Array[T => T]): Array[T] = {

    var oldResults: Array[T] = Array.fill[T](cfg.length)(sv)
    var newResults: Array[T] = Array.fill[T](cfg.length)(sv)
    var loop = 0

    // while (loop || oldResults != newResults) {
    while (loop < 15) {
      loop = loop + 1
      oldResults = newResults.clone()

      for (pc <- 0 until cfg.length) {
       // println("PC:" + pc)
        if (transformer(pc) != null && cfg(pc) != null) {
          val preds: List[Int] = cfg(pc)


          if (preds.length == 1)
            newResults(pc) = transformer(pc)(oldResults(preds(0)))
          else if (preds.length == 0)
            newResults(pc) = oldResults(pc)
          else {
            var predRes: T = sv
            for (i <- preds) {
              predRes = oldResults(i).combineWith(predRes)
            }
            newResults(pc) = transformer(pc)(predRes)
          }

        }
      }
    }
    println(oldResults.mkString("Resultarray:", "\n","."))
    return oldResults

  }


}
