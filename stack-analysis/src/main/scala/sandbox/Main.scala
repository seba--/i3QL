package sandbox

import analysis.{AnalysisResult, StackAnalysis}
import cfg.AnalysisControlFlowGraph
import sae.bytecode.bat.BATDatabaseFactory
import java.io.FileInputStream
import sae.bytecode.instructions.InstructionInfo
import sae.QueryResult
import sae.syntax.sql._
import sae.bytecode.structure.{MethodDeclaration, CodeAttribute}

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

    val methods: QueryResult[MethodDeclaration] = compile(SELECT(*) FROM database.methodDeclarations)
    val attr: QueryResult[CodeAttribute] = compile(SELECT(*) FROM database.codeAttributes) // WHERE (((_: CodeAttribute).declaringMethod.name) === "main"))
    val instr: QueryResult[InstructionInfo] = compile(SELECT(*) FROM database.instructions) // WHERE (((_: InstructionInfo).declaringMethod.name) === "main"))
    //    val query: QueryResult[(Int, Array[Int])] = compile(SELECT((codeAttribute: CodeAttribute) => (1, Array.ofDim[Int](codeAttribute.max_locals))) FROM database.codeAttributes)

    database.addClassFile(new FileInputStream("stack-analysis\\src\\main\\resources\\Test"))

    for (m <- methods) {
      val methAttr = compile(SELECT(*) FROM attr WHERE (((_: CodeAttribute).declaringMethod) === m))
      val methInstr = compile(SELECT(*) FROM instr WHERE (((_: InstructionInfo).declaringMethod) === m))

      val cfg: AnalysisControlFlowGraph = new AnalysisControlFlowGraph(methInstr.asList.sortWith((a, b) => a.sequenceIndex < b.sequenceIndex))
      val result: List[(Int, AnalysisResult)] = new StackAnalysis(cfg, methAttr.asList(0).max_locals, methAttr.asList(0).max_stack).execute().reverse

      println("Result for method '" + m.name + "'")
      println(result.mkString("\n"))
      println()

    }


  }

}
