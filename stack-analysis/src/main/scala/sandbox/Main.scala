package sandbox

import analysis.{AnalysisResult, StackAnalysis}
import CFG.AnalysisControlFlowGraph
import sae.bytecode.bat.BATDatabaseFactory
import java.io.FileInputStream
import sae.bytecode.instructions.InstructionInfo
import sae.QueryResult
import sae.syntax.sql._
import sae.bytecode.structure.CodeAttribute
import collection.mutable

/**
 * Main class
 *
 * Created with IntelliJ IDEA.
 * User: mirko
 * Date: 22.10.12
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
object Main {

  def main(args: Array[String]) {
    val database = BATDatabaseFactory.create()

    val instr: QueryResult[InstructionInfo] = compile(SELECT(*) FROM database.instructions WHERE (((_: InstructionInfo).declaringMethod.name) === "main"))
    val query: QueryResult[(Int, Array[Int])] = compile(SELECT((codeAttribute: CodeAttribute) => (1, Array.ofDim[Int](codeAttribute.max_locals))) FROM database.codeAttributes)

    database.addClassFile(new FileInputStream("C:\\Users\\Mirko\\Desktop\\testcases\\Test.class"))

    val cfg: AnalysisControlFlowGraph = new AnalysisControlFlowGraph(instr)

    val result: mutable.Map[Int, AnalysisResult] = new StackAnalysis(cfg, 0, 4).execute()

    print(result.mkString("\n"))


  }

}
