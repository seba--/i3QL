package sae.profiler


import sae._
import metrics.Metrics
import de.tud.cs.st.bat._
import sae.bytecode._
import sae.profiler.util._
import sae.util._

class ScalaCompilerDatabase extends sae.bytecode.MaterializedDatabase {

  def readBytecode: Unit = {
    addArchiveAsResource("scala-compiler.jar")
    //addArchiveAsFile("C:/Users/crypton/workspace_BA/SAE/proto-test-data/src/main/resources/scala-compiler.jar")
  }

}

object ScalaCompilerProfiler {


  implicit val iterations = 5

  def main(args: Array[String]): Unit = {
    //mvn scala:run -Dlauncher=profile
    val dbIntern = new ScalaCompilerDatabase()
    dbIntern.readBytecode

    val init: Int => Database = (x: Int) => {
      val db: Database = BATDatabaseFactory.create()
      register()(db)
      db
    }
    val profiling: Database => Unit = (x: Database) => {
      dbIntern.classfiles.foreach(e => x.classfiles.element_added(e))
      dbIntern.classfile_methods.foreach(e => x.classfile_methods.element_added(e))
      dbIntern.classfile_fields.foreach(e => x.classfile_fields.element_added(e))
      dbIntern.classes.foreach(e => x.classes.element_added(e))
      dbIntern.methods.foreach(e => x.methods.element_added(e))
      dbIntern.fields.foreach(e => x.fields.element_added(e))
      dbIntern.instructions.foreach(e => x.instructions.element_added(e))
      dbIntern.`extends`.foreach(e => x.`extends`.element_added(e))
      dbIntern.implements.foreach(e => x.implements.element_added(e))
      dbIntern.subtypes.foreach(e => x.subtypes.element_added(e))
      dbIntern.field_type.foreach(e => x.field_type.element_added(e))
      dbIntern.parameter.foreach(e => x.parameter.element_added(e))
      dbIntern.return_type.foreach(e => x.return_type.element_added(e))
      dbIntern.write_field.foreach(e => x.write_field.element_added(e))
      dbIntern.read_field.foreach(e => x.read_field.element_added(e))
      dbIntern.calls.foreach(e => x.calls.element_added(e))
      dbIntern.class_cast.foreach(e => x.class_cast.element_added(e))
      dbIntern.handled_exceptions.foreach(e => x.handled_exceptions.element_added(e))
      dbIntern.exception_handlers.foreach(e => x.exception_handlers.element_added(e))
    }
    val tearDown: Database => Unit = (x: Database) => {
      System.gc()
    }

    val times = Profiler.profile[Database](init)(profiling)(tearDown)

    times.foreach((x: Timer) => println(x.elapsedSecondsWithUnit()))
    //f1.foreach(println)
    //f2.foreach(println)
    //f3.foreach(println)
    //f4.foreach(println)
  }

  var f1: Relation[(Type, Int)] = null
  var f2: Relation[(ReferenceType, Option[Double])] = null
  var f3: Relation[(ReferenceType, Int)] = null
  var f4: Relation[(ObjectType, Int)] = null

  def register(): Database => Unit = {
    val metrics: Database => Unit = (x: Database) => {
      Metrics.numberOfFanInPerClass(x)
      Metrics.LCOMStar(x)
      Metrics.numberOfFanOutPerClass(x)
      Metrics.depthOfInheritanceTree(x)
    }
    metrics
  }
}

