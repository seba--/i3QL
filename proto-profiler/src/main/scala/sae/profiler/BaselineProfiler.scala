package sae.profiler

import sae.bytecode.BytecodeDatabase
import sae.util.Timer
import sae.profiler.Profiler._
import java.io.{FileOutputStream, PrintWriter, OutputStream}
import sae.bytecode.transform.Java6ClassTransformer
import sae.LazyView
import util.{DataQueryAnalyzer, BasicQueryAnalyzer, BasicQueryProfile, CountingObserver}

/**
 *
 * Author: Ralf Mitschke
 * Created: 16.06.11 10:41
 *
 * The baseline profiler establishes timing profiles for the following setup:
 * 1)
 * - measuring time required to pass through the base relations in a database without any registered observers
 * - measuring number of non-derived facts 'pumped' into the database
 * 2)
 * - measuring time required to compute all relations in the database
 * - measuring number of derived facts
 * - measuring operators (TODO what do we measure - which operators, size of tables etc.)
 *
 */
object BaselineProfiler
{

    def main(args: Array[String])
    {
        var out: OutputStream = System.out
        if (args.size > 0) {
            out = new FileOutputStream(args(0))
        }


        implicit val times = 0
        val writer = new PrintWriter(out, true)
        writer.println("Base Views;")
        writer.print(CSVHeader)
        writer.flush()

        measure_jar_time("hibernate-core-3.6.0.Final.jar")(readBaseViewsToCountingDatabase(_)(_))(writer)
        measure_jar_time("jedit-4.3.3-win.jar")(readBaseViewsToCountingDatabase(_)(_))(writer)
        measure_jar_time("scala-compiler-2.8.1.jar")(readBaseViewsToCountingDatabase(_)(_))(writer)
        measure_jar_time("scala-library-2.8.1.jar")(readBaseViewsToCountingDatabase(_)(_))(writer)

        writer.println("Derived Views;includes computation time for base views")
        writer.print(CSVHeader)
        writer.flush()

        println(createQueryProfile("hibernate-core-3.6.0.Final.jar").toTikZ)
        measure_jar_time("hibernate-core-3.6.0.Final.jar")(readDerivedViewsToCountingDatabase(_)(_))(writer)

        println(createQueryProfile("jedit-4.3.3-win.jar").toTikZ)
        measure_jar_time("jedit-4.3.3-win.jar")(readDerivedViewsToCountingDatabase(_)(_))(writer)

        println(createQueryProfile("scala-compiler-2.8.1.jar").toTikZ)
        measure_jar_time("scala-compiler-2.8.1.jar")(readDerivedViewsToCountingDatabase(_)(_))(writer)

        println(createQueryProfile("scala-library-2.8.1.jar").toTikZ)
        measure_jar_time("scala-library-2.8.1.jar")(readDerivedViewsToCountingDatabase(_)(_))(writer)


        writer.close()
    }

    val CSVHeader =
        """
        |jarFile; numberOfFacts; runs; min (s); max (s); mean (s); median(s);
        |""".stripMargin

    def readToPlainDatabase(jarFile: String)(run: Int) =
    {
        System.gc()
        print(jarFile + " run " + run)
        val db = new BytecodeDatabase
        println("...")
        val transformer = db.transformerForArchiveResource(jarFile)
        println("pushing data to database")
        transformer
    }

    def readBaseViewsToCountingDatabase(jarFile: String)(run: Int) =
    {
        System.gc()
        print(jarFile + " run " + run)
        val db = new BytecodeDatabase

        val o = new CountingObserver[Any]()

        db.baseViews.foreach( _.addObserver(o))

        println("...")
        val transformer = db.transformerForArchiveResource(jarFile)
        println("pushing data to database")
        (transformer, o)
    }

    def readDerivedViewsToCountingDatabase(jarFile: String)(run: Int) =
    {
        System.gc()
        print(jarFile + " run " + run)
        val db = new BytecodeDatabase

        val o = new CountingObserver[Any]()

        db.derivedViews.foreach( _.addObserver(o))

        println("...")
        val transformer = db.transformerForArchiveResource(jarFile)
        println("pushing data to database")
        (transformer, o)
    }


    def measure_jar_time(jarFile: String)(setup : (String, Int) => (Java6ClassTransformer, CountingObserver[_]))(writer: PrintWriter)(implicit times: Int = 10)
    {
        println("counting facts")
        val numberOfFacts = countFacts(jarFile)( setup(_, 0) )

        writer.print(jarFile)
        writer.print(";")
        writer.print(numberOfFacts)
        writer.print(";")
        writer.print(times)
        writer.print(";")

        val timers = profile( setup(jarFile, _) )(_._1.processAllFacts())((t: (Java6ClassTransformer,CountingObserver[_])) => {})(
            times
        )
        val median = Timer.median(timers)
        val mean = Timer.mean(timers)
        val min = Timer.min(timers)
        val max = Timer.max(timers)

        writer.print(min.elapsedSeconds)
        writer.print(";")
        writer.print(max.elapsedSeconds)
        writer.print(";")
        writer.print(mean.elapsedSeconds)
        writer.print(";")
        writer.print(median.elapsedSeconds)
        writer.print(";")

        timers.foreach((t: Timer) => writer.print(t.elapsedSeconds() + ";"))
        writer.println()
    }

    def countFacts(jarFile: String)(setup : (String) => (Java6ClassTransformer, CountingObserver[_])) =
    {
        val (transformer, observer) = setup(jarFile)
        transformer.processAllFacts()
        observer.count
    }

    def createQueryProfile(jarFile: String) =
    {
        print("profile for " + jarFile)
        val db = new BytecodeDatabase

        val analyzer = new DataQueryAnalyzer

        db.derivedViews.foreach( (view : LazyView[_]) => analyzer(view.asInstanceOf[LazyView[AnyRef]]) ) // TODO this is not nice

        println("...")
        val transformer = db.transformerForArchiveResource(jarFile)
        println("pushing data to database")
        transformer.processAllFacts()

        analyzer.profile
    }

}