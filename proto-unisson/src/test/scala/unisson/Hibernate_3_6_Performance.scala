package unisson

import hibernate_3_6.action_sad
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
        measure_action_sad
    }


    def measure_action_sad(implicit times : Int = 15) {
        println("hibernate-core-3.6.0.Final.jar")

        val timers = profile(setup_action_sad(_))(_._1.processAllFacts)(teardown_action_sad)(times)

        val median = Timer.median(timers)
        val mean = Timer.mean(timers)
        val min  = Timer.min(timers)
        val max  = Timer.max(timers)
        println("max:    " + max.elapsedSecondsWithUnit)
        println("min:    " + min.elapsedSecondsWithUnit)
        println("mean:   " + mean.elapsedSecondsWithUnit)
        println("median: " + median.elapsedSecondsWithUnit)
    }


    def setup_action_sad(run : Int) = {
        println("run " + run)
        val db = new BytecodeDatabase
        println("creating queries")
        val ensembles = new action_sad(db)
        println("reading bytecode")
        val transformer = db.transformerForArchiveResource("hibernate-core-3.6.0.Final.jar")
        println("pushing data to database")
        (transformer, ensembles)
    }

    def teardown_action_sad( data : (Java6ClassTransformer, action_sad) ) {
        import data._2._
        import sae.syntax.RelationalAlgebraSyntax._ // for implicit conversion

        incoming_engine_to_action_violation.foreach(println)

        incoming_event_to_action_violation.foreach(println)

        incoming_HQL_to_action_violation.foreach(println)

        incoming_lock_to_action_violation.foreach(println)
    }
}