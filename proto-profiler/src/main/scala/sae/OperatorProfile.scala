package sae

import bytecode.BytecodeDatabase
import bytecode.model.Instr
import bytecode.model.instructions.{invokevirtual, invokestatic, invokespecial, invokeinterface}
import java.io._
import profiler.Profiler._
import profiler.util.{CountingObserver, ObserverBuffer}
import profiler.{Profiler, Profile}
import sae.syntax.RelationalAlgebraSyntax._
import util.Timer.median
import util.{Timer, JarUtil}
import collection.mutable.Seq

/**
 *
 * Author: Ralf Mitschke
 * Created: 16.06.11 10:41
 *
 * The operator profiler establishes timing profiles for each operator:
 * Data is taken from a set of real world java programs.
 *
 *
 */
object OperatorProfile
{

    val synopsis = """OperatorProfile [outputFile]
                |% prints a csv list of timings for all derived database operators w.r.t their respective throughput
                |% optional parameters:
                |outputFile: a file where generated csv values are saved
                """.stripMargin


    def main(args: Array[String])
    {
        var out: OutputStream = System.out
        if (args.size > 0) {
            out = new FileOutputStream(args(0))
        }
        var data : Array[String] = Array()
        if( args.size > 1) {
            data = args(1).split(";")
        }

        implicit var times = 100
        if( args.size > 2) {
            times = Integer.parseInt(args(2))
        }



        implicit val writer = new PrintStream(out, true)
        writer.flush()


        for( jar <- data) {
            measureCodebase(jar, jar)
        }

        /*
        measureCodebase("geronimo-jpa_1.0_spec-1.1.2", "geronimo-jpa_1.0_spec-1.1.2.jar")

        measureCodebase("jedit-4.3.3", "jedit-4.3.3-win.jar")
*/

        //measureCodebase("hibernate-core-3.6", "hibernate-core-3.6.0.Final.jar")

        // measureCodebase("scala-compiler-2.8.1", "scala-compiler-2.8.1.jar")



        writer.close()
    }

    def measureCodebase(name: String, uri: String, uris: String*)(implicit times: Int, writer: PrintStream = System.out)
    {
        val urls = JarUtil.resolveDirectoryAndJarUrisFromClasspath(Array(uri) ++ uris)

        val db = new BytecodeDatabase

        val instructions = new ObserverBuffer[Instr[_]](db.instructions)

        val counter = new CountingObserver[Instr[_]]
        db.instructions.addObserver(counter)

        implicit val profiles = new scala.collection.mutable.ListBuffer[Profile]

        db.transformerForArchiveStreams(urls.filter(_.getFile.endsWith("jar")).map(_.openStream)).processAllFacts()

        // no relations are stored except in the buffer

        val baselineTimers = profileOperation({}, instructions.replay(), reset(instructions))
        reportTimerProfile(baselineTimers, counter.count, name, "just replay")

        //writer.println(name + ";average replay time (ns/call);" + average(baselineTimers, counter.count))


        // we had twice as many passthrough call, since we used as union with on and the same relation
        val passthroughTimers = profileOperation(passthroughOperation(instructions), instructions.replay(), reset(instructions))
        reportTimerProfile(passthroughTimers, counter.count, name, "whole relation union")

        //writer.println(name + ";average union operator time (ns/call);" + average(passthroughTimers, counter.count))
        //writer.println(name + ";average method call time (ns/call);" + average(passthroughTimers, counter.count * 2, baselineTimers))


        val selectEverythingOperationTimers = profileOperation(selectEverythingOperation(instructions), instructions.replay(), reset(instructions))
        reportTimerProfile(selectEverythingOperationTimers, counter.count, name, "select where true")

        val selectNoneOperationTimers = profileOperation(selectNoneOperation(instructions), instructions.replay(), reset(instructions))
        reportTimerProfile(selectNoneOperationTimers, counter.count, name, "select where false")

        val selectCallInstructionsInstanceOfOperationTimers = profileOperation(selectCallInstructionsInstanceOfOperation(instructions), instructions.replay(), reset(instructions))
        reportTimerProfile(selectCallInstructionsInstanceOfOperationTimers, counter.count, name, "select where instanceof Call")

        val selectCallInstructionsPatternMatchOperationTimers = profileOperation(selectCallInstructionsPatternMatchOperation(instructions), instructions.replay(), reset(instructions))
        reportTimerProfile(selectCallInstructionsPatternMatchOperationTimers, counter.count, name, "select where instanceof Call -- with pattern match")

        val joinWholeInstructionRelationsTimers = profileOperation(joinWholeRelations(instructions), instructions.replay(), reset(instructions))
        reportTimerProfile(joinWholeInstructionRelationsTimers, counter.count, name, "whole relation join")


        // print the whole thing

        printProfilesAsCSV(profiles, writer)
    }

    def printProfilesAsCSV(profiles : Seq[Profile],  writer: PrintStream) {
        writer.print("codeBase;")
        profiles.foreach( (p:Profile) => writer.print( p.codeBase + ";") )
        writer.println()

        writer.print("profileName;")
        profiles.foreach( (p:Profile) => writer.print( p.profileName + ";") )
        writer.println()

        writer.print("sampleSize;")
        profiles.foreach( (p:Profile) => writer.print( p.sampleSize + ";") )
        writer.println()

        writer.print("median (ns);")
        profiles.foreach( (p:Profile) => writer.print( Timer.median(p.timers).elapsedNanoSeconds() + ";") )
        writer.println()

        writer.print("mean (ns);")
        profiles.foreach( (p:Profile) => writer.print( Timer.mean(p.timers).elapsedNanoSeconds() + ";") )
        writer.println()

        writer.print("min (ns);")
        profiles.foreach( (p:Profile) => writer.print( Timer.mean(p.timers).elapsedNanoSeconds() + ";") )
        writer.println()

        writer.print("max (ns);")
        profiles.foreach( (p:Profile) => writer.print( Timer.max(p.timers).elapsedNanoSeconds() + ";") )
        writer.println()

        writer.print("data (ns);")
        profiles.foreach( (p:Profile) => writer.print( "(" + p.measurements + " runs);") )
        writer.println()

        // print the data
        val dataSize = profiles.map( _.measurements).max
        var i = 0
        while( i < dataSize ) {
            writer.print(i + ";")
            profiles.foreach( (p:Profile) =>
                    if( i < p.measurements){
                        writer.print(p.timers(i).elapsedNanoSeconds() + ";")
                    }
                    else{
                        writer.print(";")
                    }
            )
            writer.println()
            i = i + 1
        }
    }


    def reset(buffers : ObserverBuffer[_]*) {
        buffers.foreach(_.resetObservers())
        System.gc()
    }

    def average(timers : Array[Timer], workload : Long, baseline : Array[Timer]): Double = (median(timers).elapsedNanoSeconds() - median(baseline).elapsedNanoSeconds()).asInstanceOf[Double] /  (workload)

    def average(timers : Array[Timer], workload : Long): Double = median(timers).elapsedNanoSeconds() / workload

/*
    def reportTimerProfile(timers: Array[Timer], sampleSize: Int, codeBase: String, profileName: String)(implicit writer: PrintStream = System.out)
    {
        writer.print(codeBase)
        writer.print(";")
        writer.print(profileName)
        writer.print(";")

        writer.print(sampleSize)
        writer.print(";")


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

        writer.println()
        writer.println("raw data")
        timers.foreach((t: Timer) => writer.println(t.elapsedNanoSeconds()))
        writer.println()
    }
*/
   def reportTimerProfile(timers: Array[Timer], sampleSize: Int, codeBase: String, profileName: String)(implicit profiles : scala.collection.mutable.ListBuffer[Profile])
    {
        println(codeBase + " -- " + profileName + " -- DONE")
        profiles.append(Profile(codeBase, profileName, timers, sampleSize))
    }

    def profileOperation(setup: => Unit, replay: => Unit, tearDown: => Unit)(implicit times: Int) : Array[Timer] =
        profile[Unit]((i:Int) => setup)((t:Unit) => replay)((t:Unit) => tearDown)

    def passthroughOperation[T <: AnyRef](view: LazyView[T]): LazyView[T] = view.∪[T, T](view)

    def joinWholeRelations[T <: AnyRef](view: LazyView[T]): LazyView[T] = ((view, identity(_:T)) ⋈ (identity(_:T), view)){ (t1:T, t2:T) => t1 }

    def selectEverythingOperation[T <: AnyRef](view: LazyView[T]): LazyView[T] = σ( (_:T) => true)(view)

    def selectNoneOperation[T <: AnyRef](view: LazyView[T]): LazyView[T] = σ( (_:T) => false)(view)

    def selectCallInstructionsPatternMatchOperation(view: LazyView[Instr[_]]): LazyView[Instr[_]] = σ((_: Instr[_]) match {
        case invokeinterface(_, _, _) => true
        case invokespecial(_, _, _) => true
        case invokestatic(_, _, _) => true
        case invokevirtual(_, _, _) => true
        case _ => false
    })(view)

    def selectCallInstructionsInstanceOfOperation(view: LazyView[Instr[_]]): LazyView[Instr[_]] = σ((i: Instr[_]) => {
        i.isInstanceOf[invokeinterface] ||
        i.isInstanceOf[invokespecial] ||
        i.isInstanceOf[invokestatic] ||
        i.isInstanceOf[invokevirtual]
    })(view)
}