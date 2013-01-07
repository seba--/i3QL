package sandbox

import findbugs.StackBugAnalysis
import sae.bytecode.bat.BATDatabaseFactory
import java.io.{File, FileInputStream}
import stackAnalysis.BytecodeTransformer
import stackAnalysis.codeInfo.StackAnalysis
import stackAnalysis.datastructure.State
import stackAnalysis.instructionInfo.ControlFlowGraph
import sae.syntax.RelationalAlgebraSyntax.TC


/**
 * Main class
 */
object Main {

  def main(args: Array[String]) {


    val database = BATDatabaseFactory.create()

    StackAnalysis.printResults = true
    StackBugAnalysis.printResults = true
    StackBugAnalysis(database)


    val a = TC(ControlFlowGraph(database))(
      ce => ce.previous,
      ce => {
        val result = ce.next
        if (ce.previous.instruction != null) {
          println("Bef: " + result)
          val previousTransformed: State = BytecodeTransformer.getTransformer(ce.previous.instruction.pc, ce.previous.instruction.instruction)(ce.previous.getState)
          result.setState(ce.next.getState.combineWith(previousTransformed))
        }
        println("Res: " + result)

        result
      }
    )




    //  def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")
    //  database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    database.addClassFile(new FileInputStream("stack-analysis" + File.separator + "target" + File.separator + "test-classes" + File.separator + "TestTransitive.class"))

    //println(a.foreach[Unit]((p) => println(p._2)))

    println("###########################################")
    println(a.graph);
    println(a.transitiveClosure)
  }


}
