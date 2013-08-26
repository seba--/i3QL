package sandbox

import findbugs.detect.{RC_REF_COMPARISON, SA_LOCAL_SELF_ASSIGNMENT}
import findbugs.BugAnalysis
import sae.bytecode.bat.{BATBytecodeDatabase, BATDatabaseFactory}
import java.io.{File, FileInputStream}
import stackAnalysis.{StackAnalysis, StateTransformer}
import stackAnalysis.codeInfo.{MethodResultToStateInfoView, CIStackAnalysis}
import stackAnalysis.datastructure.State
import stackAnalysis.instructionInfo.{IIStackAnalysis, AnchorControlFlowEdge, IIControlFlowAnalysis}
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


    val stateInfo: QueryResult[_] = StackAnalysis.byInstructionInfo(database)
    //val a: QueryResult[_] = new MethodResultToStateInfoView(database.code,CIStackAnalysis(database) )
    val bugInfo: QueryResult[_] = RC_REF_COMPARISON.byInstructionInfo(database)

    //  def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")
    //  database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    database.addClassFile(new FileInputStream("stack-analysis" + File.separator + "target" + File.separator + "test-classes" + File.separator + "TestTransitive.class"))

    println("StateInfo-----------------------------------------------------------------------------------------")
    println(stateInfo.foreach[Unit]((p) => println(p)))

    println("BugInfo-----------------------------------------------------------------------------------------")
    println(bugInfo.foreach[Unit]((p) => println(p)))

  }


}
