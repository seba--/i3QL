package sae.profiler

import sae.bytecode._
import de.tud.cs.st.lyrebird.replayframework.Replay
import java.io.File
import sae.profiler.util._
import sae.LazyView
import sae.bytecode.model._
import sae.bytecode.model.dependencies._
import sae.operators._
import sae.syntax._
import sae._
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.Type
import de.tud.cs.st.bat.ObjectType
import de.tud.cs.st.bat.ReferenceType
import sae.functions.Count
import sae.collections.QueryResult
import sae.test.helpFunctions.ObserverList
import sae.functions.Sum
import sae.functions.CalcSelfMaintable

object ProfileVespucciEventset {
  var tmp: QueryResult[(ObjectType, ObjectType)] = null
  var tmp2 : QueryResult[(ObjectType, Int)] = null
  //var tmp3 : ObserverList[(ReferenceType,Int,Int,Int)] = new ObserverList()
  //    var tmp3 : ObserverList[(ReferenceType, Option[Double])] = new ObserverList()
  //private val replay = new Replay(new File("./src/main/resources/VespucciEventSet"))
  def main(args: Array[String]): Unit = {
    //initial settings
    //val replayHelper = new ReplayHelper(new File("./src/main/resources/VespucciEventSet"))
    //val replayHelper = new ReplayHelper(new File("./src/main/resources/Flashcards 0.3/bin"))
    //val replayHelper = new ReplayHelper(new File("./src/main/resources/testSet/bin"))
     val replayHelper = new ReplayHelper(new File("./src/main/resources/extendsSet/bin"))
    register(replayHelper.buffer)

    replayHelper.applyAll
     tmp.foreach(println _)
     println("--")
    tmp2.foreach(println _)
    //        println("--")
    //        tmp3.data.foreach(println _)
    //        println("--")
    //        tmp.foreach(println _)
  }

  /**
   *
   */
  def register(dbBuffer: DatabaseBuffer) {  }
    import sae.metrics.Metrics._
    //f(dbBuffer.parameter, dbBuffer.classfile_methods, dbBuffer.classfile_fields)
    //registerFanIn(dbBuffer.parameter, dbBuffer.classfile_methods, dbBuffer.classfile_fields)
    //registerLCOMStar(dbBuffer.read_field, dbBuffer.write_field, dbBuffer.classfile_methods, dbBuffer.classfile_fields)
    //tmp = registerExtends(dbBuffer.`extends`)
    //tmp2 = registerExtendsCount(dbBuffer.`extends`)
    //registerExtendsCount(dbBuffer.`extends`)




}