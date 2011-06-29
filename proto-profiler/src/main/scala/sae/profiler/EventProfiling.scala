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
import metrics.Metrics
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.Type
import de.tud.cs.st.bat.ObjectType
import de.tud.cs.st.bat.ReferenceType
import sae.functions.Count
import sae.collections.QueryResult
import sae.test.helpFunctions.ObserverList
import sae.functions.Sum

import sae.util.Timer

object EventProfiling {

  //mvn scala:run -Dlauncher=event-profiler
  def main(args: Array[String]): Unit = {
    if (args.size == 0) throw new Error("Specifiy EventSet location")

    //initial settings
    // val replayHelper = new EventProfileHelper(new File("./src/main/resources/VespucciEventSet"))
    //val replayHelper = new EventProfileHelper(new File("./src/main/resources/Flashcards 0.3/bin"))
    //val replayHelper = new EventProfileHelper(new File("./src/main/resources/testSet/bin"))
    //val replayHelper = new EventProfileHelper(new File("./src/main/resources/extendsSet/bin"))
    println("Start Profling for: " + args(0))
    val replayHelper = new EventProfileHelper(new File(args(0)), register())
    val init: () => Unit = () => {
      replayHelper.init
    }

    val times: Array[Array[sae.util.Timer]] = Profiler.profile2[DatabaseBuffer](
      init, //initSetup
      (x: Int) => {
        replayHelper.beforeMeasurement(x)
      }, //beforeMeasurement
      (x: DatabaseBuffer) => {
        x.replay()
      }, //f
      (x: DatabaseBuffer) => {
        x.reset()
      }, //afterMeasurement
      () => {
        System.gc()
      }, //tearDown
      replayHelper.size, //countF
      10 //times
    )

    for (i <- 0 until times.length) {
      //for(j <- 0 until times(i).length)
      // println("i: " + i + " j: " + j + " time: " + times(i)(j).elapsedMilliSeconds())
      println(replayHelper.getInfo(i))
      println("Min: " + Timer.min(times(i)).elapsedMilliSecondsWithUnit() + " Max: " + Timer.max(times(i)).elapsedMilliSecondsWithUnit() + " Mean: " + Timer.mean(times(i)).elapsedMilliSecondsWithUnit()
        + " Median: " + Timer.median(times(i)).elapsedMilliSecondsWithUnit())
    }


  }


  var f1: LazyView[(Type,Int)] = null
  var f2: LazyView[(ReferenceType,Option[Double])] = null
  var f3: LazyView[(ReferenceType,Int)] = null
  var f4: LazyView[(ObjectType,Int)] = null

  /**
   *
   */
  def register(): Database => Unit = {
    val metrics: Database => Unit = (x: Database) => {
      f1 = Metrics.getFanIn(x)
      f2 = Metrics.getLCOMStar(x)
      f3 = Metrics.getFanOut(x)
      f4 = Metrics.getDedthOfInheritanceTree(x)
    }
    metrics
  }
}