package sae

import bytecode.BytecodeDatabase
import bytecode.model.Instr
import bytecode.model.instructions.{invokevirtual, invokestatic, invokespecial, invokeinterface}
import java.io._
import profiler.Profiler._
import profiler.util.{CountingObserver, ObserverBuffer}
import sae.syntax.RelationalAlgebraSyntax._
import util.Timer.median
import util.{Timer, JarUtil}
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




        implicit val times = 1000
        implicit val writer = new PrintStream(out, true)
        writer.flush()


        measureCodebase("hibernate-core-3.6", "hibernate-core-3.6.0.Final.jar")

        writer.close()
    }

    def measureCodebase(name: String, uri: String, uris: String*)(implicit times: Int, writer: PrintStream = System.out)
    {
        val urls = JarUtil.resolveDirectoryAndJarUrisFromClasspath(Array(uri) ++ uris)

        val db = new BytecodeDatabase

        val instructions = new ObserverBuffer[Instr[_]](db.instructions)

        val counter = new CountingObserver[Instr[_]]
        db.instructions.addObserver(counter)

        db.transformerForArchiveStreams(urls.filter(_.getFile.endsWith("jar")).map(_.openStream)).processAllFacts()

        // no relations are stored except in the buffer

        val baselineTimers = profileOperation({}, instructions.replay)
        reportTimerProfile(baselineTimers, counter.count, name, "just replay")

        writer.println(name + ";average replay time (ns/call);" + average(baselineTimers, counter.count))


        // we had twice as many passthrough call, since we used as union with on and the same relation
        val passthroughTimers = profileOperation(passthroughOperation(instructions), instructions.replay)
        reportTimerProfile(passthroughTimers, counter.count * 2, name, "method call throughput")

        writer.println(name + ";average union operator time (ns/call);" + average(passthroughTimers, counter.count))
        writer.println(name + ";average method call time (ns/call);" + average(passthroughTimers, counter.count * 2, baselineTimers))
    }

    def measureSelection( baseline : Array[Timer]) {

    }


    def average(timers : Array[Timer], workload : Int, baseline : Array[Timer]): Double = (median(timers).elapsedNanoSeconds() - median(baseline).elapsedNanoSeconds()).asInstanceOf[Double] /  (workload)

    def average(timers : Array[Timer], workload : Int): Double = median(timers).elapsedNanoSeconds() / workload


    def reportTimerProfile(timers: Array[Timer], workSteps: Int, codeBase: String, profileName: String)(implicit writer: PrintStream = System.out)
    {


        writer.print(codeBase)
        writer.print(";")
        writer.print(profileName)
        writer.print(";")

        writer.print(workSteps)
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


    def profileOperation(registerObservers: => Unit, replay: => Unit)(implicit times: Int) =
    {
        System.gc()

        registerObservers

        profile(replay)
    }

    def passthroughOperation[T <: AnyRef](view: LazyView[T]): LazyView[T] = view.∪[T, T](view)

    def selectEverythingOperation[T <: AnyRef](view: LazyView[T]): LazyView[T] = σ(_ => true)(view)

    def selectNoneOperation[T <: AnyRef](view: LazyView[T]): LazyView[T] = σ(_ => false)(view)

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