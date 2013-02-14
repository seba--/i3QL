package sandbox

import findbugs.{IIStackBugAnalysis, CIStackBugAnalysis}
import sae.bytecode.bat.{BATBytecodeDatabase, BATDatabaseFactory}
import java.io.{File, FileInputStream}
import stackAnalysis.StateTransformer
import stackAnalysis.codeInfo.CIStackAnalysis
import stackAnalysis.datastructure.State
import stackAnalysis.instructionInfo.{IIStackAnalysis, InstructionState, AnchorControlFlowEdge, IIControlFlowAnalysis}
import sae.syntax.RelationalAlgebraSyntax.TC
import sae.operators.impl._
import sae.bytecode.instructions.InstructionInfo
import sae.{QueryResult, Relation}
import sae.syntax.sql._


/**
 * Main class
 */
object Main {


  def main(args: Array[String]) {


    val database: BATBytecodeDatabase = BATDatabaseFactory.create().asInstanceOf[BATBytecodeDatabase]

    IIStackBugAnalysis.printResults = true


    val a: QueryResult[_] = IIStackBugAnalysis(database)

    //  def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")
    //  database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    database.addClassFile(new FileInputStream("stack-analysis" + File.separator + "target" + File.separator + "test-classes" + File.separator + "TestMethods.class"))

    println(a.foreach[Unit]((p) => println(p)))

  }


}
