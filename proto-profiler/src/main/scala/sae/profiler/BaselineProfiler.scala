package sae.profiler

import sae.bytecode.BytecodeDatabase
import sae.util.Timer
import sae.profiler.Profiler._
import util.CountingObserver
import java.io.{FileOutputStream, PrintWriter, OutputStream}
import sae.bytecode.transform.Java6ClassTransformer
import sae.Observer

/**
 *
 * Author: Ralf Mitschke
 * Created: 16.06.11 10:41
 *
 * The baseline profiler establishes timing profiles for the following setup:
 * - measuring time required to pass through the base relations in a database without any registered observers
 * - measuring number of facts 'pumped' into the database
 */
object BaselineProfiler
{

    def main(args: Array[String])
    {
        var out: OutputStream = System.out
        if (args.size > 0) {
            out = new FileOutputStream(args(0))
        }


        implicit val times = 10
        val writer = new PrintWriter(out, true)
        writer.print(CSVHeader)
        writer.flush()

        measure_jar("hibernate-core-3.6.0.Final.jar")(writer)
        measure_jar("jedit-4.3.3-win.jar")(writer)
        measure_jar("scala-compiler-2.8.1.jar")(writer)
        measure_jar("scala-library-2.8.1.jar")(writer)

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

    def readToCountingDatabase(jarFile: String)(run: Int) =
    {
        System.gc()
        print(jarFile + " run " + run)
        val db = new BytecodeDatabase

        val o = new CountingObserver[AnyRef]()
        db.classfiles.addObserver(o)
        db.classfile_methods.addObserver(o)
        db.classfile_fields.addObserver(o)
        db.classes.addObserver(o)
        db.methods.addObserver(o)
        db.fields.addObserver(o)
        db.instructions.addObserver(o)
        db.`extends`.addObserver(o)
        db.implements.addObserver(o)
        db.internal_inner_classes.addObserver(o)
        db.internal_enclosing_methods.addObserver(o)
        db.parameter.addObserver(o)

        println("...")
        val transformer = db.transformerForArchiveResource(jarFile)
        println("pushing data to database")
        (transformer, o)
    }

    def measure_jar(jarFile: String)(writer: PrintWriter)(implicit times: Int = 10)
    {
        println("counting facts")
        val numberOfFacts = countFacts(jarFile)

        writer.print(jarFile)
        writer.print(";")
        writer.print(numberOfFacts)
        writer.print(";")
        writer.print(times)
        writer.print(";")
        // reading a plain db or a counting one makes no difference
        val timers = profile(readToCountingDatabase(jarFile)(_))(_._1.processAllFacts())((t: (Java6ClassTransformer,Observer[AnyRef])) => {})(
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

    def countFacts(jarFile: String) =
    {
        val (transformer, observer) = readToCountingDatabase(jarFile)(0)
        transformer.processAllFacts()
        observer.count
    }
}