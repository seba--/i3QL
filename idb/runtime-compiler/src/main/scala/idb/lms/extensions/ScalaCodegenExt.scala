package idb.lms.extensions


import scala.reflect.RefinedManifest
import scala.virtualization.lms.internal.{ScalaFatCodegen, ScalaCodegen}
import scala.tools.nsc.{Settings, Global}
import scala.tools.nsc.reporters.ConsoleReporter
import java.io.{StringWriter, PrintWriter}
import scala.reflect.io.{File, VirtualDirectory}
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.virtualization.lms.common.{FunctionsExp, BaseExp}

/**
 *
 * @author Ralf Mitschke
 */
trait ScalaCodegenExt
    extends ScalaCodegen
{

    val IR: BaseExp with FunctionsExp


    var compiler: Global = _
    var reporter: ConsoleReporter = _

    def setupCompiler () {
        val settings = new Settings ()

        val pathSeparator = System.getProperty("path.separator")

        // we use the environment rather than the current class loader to work with maven surefire
        settings.classpath.value = this.getClass.getClassLoader match {
          case ctx: java.net.URLClassLoader => ctx.getURLs.map(_.getPath).mkString(pathSeparator)
          case _ => System.getProperty("java.class.path")
        }

        settings.bootclasspath.value = Predef.getClass.getClassLoader match {
            case ctx: java.net.URLClassLoader => ctx.getURLs.map (_.getPath).mkString (File.pathSeparator)
            case _ => System.getProperty ("sun.boot.class.path")
        }
        settings.encoding.value = "UTF-8"
        settings.outdir.value = "."
        settings.extdirs.value = ""
        settings.Ylogcp.value = false

        reporter = new ConsoleReporter (settings, null, new PrintWriter (System.out)) //writer
        compiler = new Global (settings, reporter)
    }

	var compileCount = 0

    var dumpGeneratedCode = true

    var silent = true

    def compileFunctionApplied[A: Manifest, B: Manifest] (f: IR.Rep[A => B]): A => B = {
        compileFunction (IR.doApply (f, _))
    }

    override def remap[A](m: Manifest[A]): String = m match {
        case rm: RefinedManifest[A] =>  super.remap(m)
        case _ if m.erasure == classOf[IR.Variable[Any]] => super.remap(m)
        case _ if m.typeArguments.size > 0 =>
            m.toString()// + m.typeArguments.map(t => remap(t)).mkString("[",",","]")

        /*    // call remap on all type arguments
            val targs = m.typeArguments
            if (targs.length > 0) {
                val ms = m.toString
                ms.take(ms.indexOf("[")+1) + targs.map(tp => remap(tp)).mkString(", ") + "]"
            }
            else m.toString  */
        case _ => super.remap(m)
    }


    def compileFunctionWithDynamicManifests[A, B] (f: IR.Rep[A => B]): A => B = {

        f.tp.typeArguments match {
            case List (mA, mB) => {
                val mAUnsafe = mA.asInstanceOf[Manifest[A]]
                val mBUnsafe = mB.asInstanceOf[Manifest[B]]
                compileFunction (
                    IR.doApply (f, _: IR.Rep[A])(
                        mAUnsafe,
                        mBUnsafe,
                        if (!f.pos.isEmpty)
                            f.pos (0)
                        else
                            null
                    )
                )(mAUnsafe, mBUnsafe)
            }
        }
    }

    def compileFunction[A: Manifest, B: Manifest] (f: IR.Rep[A] => IR.Rep[B]): A => B = {
        if (this.compiler eq null)
            setupCompiler ()

        val className = "staged$" + compileCount
        compileCount += 1

        val source = new StringWriter ()
        val staticData = emitSource (f, className, new PrintWriter (source))
        // IR.reset

        if (dumpGeneratedCode) println (source)

        val compiler = this.compiler
        val run = new compiler.Run

        val fileSystem = new VirtualDirectory ("<vfs>", None)
        compiler.settings.outputDirs.setSingleOutput (fileSystem)

        run.compileSources (List (new scala.reflect.internal.util.BatchSourceFile ("<stdin>", source.toString)))


        if (!silent) {
            reporter.printSummary ()
            if (!reporter.hasErrors)
                println ("compilation: ok")
            else
                println ("compilation: had errors")
        }

        reporter.reset ()

        val loader = new AbstractFileClassLoader (fileSystem, this.getClass.getClassLoader)

        val cls: Class[_] = loader.loadClass (className)
        val cons = cls.getConstructor (staticData.map (_._1.tp.runtimeClass): _*)

        val obj: A => B = cons.newInstance (staticData.map (_._2.asInstanceOf[AnyRef]): _*).asInstanceOf[A => B]
        obj
    }

}
