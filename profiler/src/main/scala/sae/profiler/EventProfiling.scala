package sae.profiler

import sae.bytecode._

import java.io.File
import sae.profiler.util._
import sae.metrics._
import sae.util.Timer
import de.tud.cs.st.bat.ReferenceType

import sae.collections.QueryResult


object EventProfiling {

  //mvn scala:run -Dlauncher=event-profiler
  def main(args: Array[String]): Unit = {
    if (args.size == 0) throw new Error("Specifiy EventSet location")
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
      3 //times
    )

    for (i <- 0 until times.length) {
      //for(j <- 0 until times(i).length)
      // println("i: " + i + " j: " + j + " time: " + times(i)(j).elapsedMilliSeconds())
      print("\n"+ replayHelper.getInfo(i))
      print("\tMin: " + Timer.min(times(i)).elapsedMilliSecondsWithUnit() + "\tMax: " + Timer.max(times(i)).elapsedMilliSecondsWithUnit() + "\tMean: " + Timer.mean(times(i)).elapsedMilliSecondsWithUnit()   + "\t\tMedian: " + Timer.median(times(i)).elapsedMilliSecondsWithUnit())
    }
    println("")
    //f2.foreach(println)

  }

   import sae.syntax.RelationalAlgebraSyntax._
  //var f1: LazyView[(Type,Int)] = null
  var f2: QueryResult[(ReferenceType,Option[Double])] = null
  //var f3: LazyView[(ReferenceType,Int)] = null
  //var f4: LazyView[(ObjectType,Int)] = null
  //var f5: LazyView[(Method, Field)] = null
  //var f6 : QueryResult[Field] = null
  //var f7 : LazyView[(String, Int)] = null

 // var obBuffer : ObserverBuffer[(Method, Field)] = null
  /**
   *
   */
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

