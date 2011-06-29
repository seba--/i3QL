package sae.profiler

import sae.profiler.util.Profile
import sae._
import bytecode.{BytecodeDatabase, Database}
import metrics.Metrics
import sae.operators._
import sae.functions._
import sae.util._
import sae.bytecode.model._
import sae.syntax.RelationalAlgebraSyntax._
import sae.test.helpFunctions._
import com.google.common.collect.HashMultiset
import scala.collection.mutable._
import de.tud.cs.st.bat._


class ScalaCompilerDatabase extends sae.bytecode.MaterializedDatabase {

  def readBytecode: Unit = {
    addArchiveAsResource("scala-compiler.jar")
    //addArchiveAsFile("C:/Users/crypton/workspace_BA/SAE/proto-test-data/src/main/resources/scala-compiler.jar")
  }

}

object ScalaCompilerProfiler {


  implicit val iterations = 5

  def main(args: Array[String]): Unit = {
    val dbIntern = new ScalaCompilerDatabase()
    dbIntern.readBytecode

    val init: Int => Database = (x: Int) => {
      val db: Database = new BytecodeDatabase()
      register()(db)
      db
    }
    val profiling: Database => Unit = (x : Database) => {
         //dbIntern.
    }
    val tearDown: Database => Unit =  (x : Database) =>{System.gc()}

    val times = Profiler.profile[Database](init)(profiling)(tearDown)
  }


  def register(): Database => Unit = {
    val metrics: Database => Unit = (x: Database) => {
      Metrics.getFanIn(x)
      Metrics.getLCOMStar(x)
      Metrics.getFanOut(x)
      Metrics.getDedthOfInheritanceTree(x)
    }
    metrics
  }
}

