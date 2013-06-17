package idb.iql.lms.extensions

import scala.virtualization.lms.internal.ScalaCodegen
import scala.tools.nsc.{util, Settings, Global}
import scala.tools.nsc.reporters.ConsoleReporter
import java.io.{StringWriter, PrintWriter}
import scala.reflect.io.{File, VirtualDirectory}
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.virtualization.lms.common.{FunctionsExp, BaseExp}

/**
 *
 * @author Ralf Mitschke
 */
trait CompileScalaExt
    extends ScalaCodegen
{

    val IR: BaseExp with FunctionsExp

    var compiler: Global = _
    var reporter: ConsoleReporter = _

    def setupCompiler () = {
        val settings = new Settings ()

        // we use the environment rather than the current class loader to work with maven surefire
        settings.classpath.value = System.getProperty ("java.class.path")
        settings.bootclasspath.value = Predef.getClass.getClassLoader match {
            case ctx: java.net.URLClassLoader => ctx.getURLs.map (_.getPath).mkString (File.pathSeparator)
            case _ => System.getProperty ("sun.boot.class.path")
        }
        settings.encoding.value = "UTF-8"
        settings.outdir.value = "."
        settings.extdirs.value = ""
        settings.Ylogcp.value = true
        //settings.verbose.value = true
        // -usejavacp needed on windows?

        reporter = new ConsoleReporter (settings, null, new PrintWriter (System.out)) //writer
        compiler = new Global (settings, reporter)
    }

    var compileCount = 0

    var dumpGeneratedCode = true

    var silent = false

    def compileApplied[A: Manifest, B: Manifest] (f: IR.Rep[A => B]): A => B =
        compileFunction (IR.doApply (f, _))

    def compileFunction[A: Manifest, B: Manifest] (f: IR.Rep[A] => IR.Rep[B]): A => B = {
        if (this.compiler eq null)
            setupCompiler ()

        val className = "staged$" + compileCount
        compileCount += 1

        val source = new StringWriter ()
        val staticData = emitSource (f, className, new PrintWriter (source))

        if (dumpGeneratedCode) println (source)

        val compiler = this.compiler
        val run = new compiler.Run

        val fileSystem = new VirtualDirectory ("<vfs>", None)
        compiler.settings.outputDirs.setSingleOutput (fileSystem)

        run.compileSources (List (new util.BatchSourceFile ("<stdin>", source.toString)))


        if (!silent) {
            reporter.printSummary ()
            if (!reporter.hasErrors)
                println ("compilation: ok")
            else
                println ("compilation: had errors")
        }

        reporter.reset

        val parent = this.getClass.getClassLoader
        val loader = new AbstractFileClassLoader (fileSystem, this.getClass.getClassLoader)

        val cls: Class[_] = loader.loadClass (className)
        val cons = cls.getConstructor (staticData.map (_._1.tp.runtimeClass): _*)

        val obj: A => B = cons.newInstance (staticData.map (_._2.asInstanceOf[AnyRef]): _*).asInstanceOf[A => B]
        obj
    }

}
