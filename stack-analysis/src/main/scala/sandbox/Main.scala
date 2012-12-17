package sandbox

import findbugs.StackBugAnalysis
import sae.bytecode.bat.BATDatabaseFactory
import java.io.{File, FileInputStream}
import stackAnalysis.codeInfo.StackAnalysis


/**
 * Main class
 */
object Main {

  def main(args: Array[String]) {


    val database = BATDatabaseFactory.create()

    StackAnalysis.printResults = true
    StackBugAnalysis.printResults = true
    StackBugAnalysis(database)


    /*  val a = TC(ControlFlowGraph(database))(
      ce => ce.previous,
      ce => ControlFlowVertex(ce.next.instruction,BytecodeTransformer.getTransformer(ce.next.instruction.pc,ce.next.instruction.instruction)(ce.previous.state)))
    */

    //  def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")
    //  database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    database.addClassFile(new FileInputStream("stack-analysis" + File.separator + "target" + File.separator + "test-classes" + File.separator + "TestMethods.class"))

    //println(a.foreach[Unit]((p) => println(p._2)))
  }


}
