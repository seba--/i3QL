package unisson

import hibernate_3_6._
import sae.bytecode.BytecodeDatabase
import sae.util.Timer
import sae.profiler.Profiler._
import sae.bytecode.transform.Java6ClassTransformer
import scala.Array

/**
 * 
 * Author: Ralf Mitschke
 * Created: 07.06.11 10:09
 *
 */

object Hibernate_3_6_Performance {

    def main(args : Array[String]) {
        println("measuring performance:")
        measure_sad(new action_sad(_))
        measure_sad(new bytecode_sad(_))
    }

    def setup_ensemble_definition( f : BytecodeDatabase => EnsembleDefinition)(run : Int) = {
        println("run " + run)
        val db = new BytecodeDatabase
        println("creating queries")
        val ensembles = f(db)
        println("reading bytecode")
        val transformer = db.transformerForArchiveResource("hibernate-core-3.6.0.Final.jar")
        println("pushing data to database")
        (transformer, ensembles)
    }

    def teardown_ensemble_definition( data : (Java6ClassTransformer, EnsembleDefinition) ) {
        data._2.printViolations()
    }

    def measure_sad(f : BytecodeDatabase => EnsembleDefinition)(implicit times : Int = 15) {
        println("hibernate-core-3.6.0.Final.jar")

        val timers = profile(setup_ensemble_definition(f)(_))(_._1.processAllFacts)(teardown_ensemble_definition)(times)

        val median = Timer.median(timers)
        val mean = Timer.mean(timers)
        val min  = Timer.min(timers)
        val max  = Timer.max(timers)
        println("max:    " + max.elapsedSecondsWithUnit)
        println("min:    " + min.elapsedSecondsWithUnit)
        println("mean:   " + mean.elapsedSecondsWithUnit)
        println("median: " + median.elapsedSecondsWithUnit)
        timers.sorted.foreach( (t:Timer) => print(t.elapsedSeconds() + ";") )
    }

}