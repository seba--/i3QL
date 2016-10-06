package idb.lms.extensions


import scala.reflect.RefinedManifest
import scala.virtualization.lms.internal.{ScalaCodegen, ScalaFatCodegen}
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.ConsoleReporter
import java.io.{PrintWriter, StringWriter}

import idb.algebra.compiler.util.ClassCode

import scala.reflect.io.{File, VirtualDirectory}
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.virtualization.lms.common.{BaseExp, FunctionsExp}


/**
 *
 * @author Ralf Mitschke
 */
trait ScalaCodegenExt
    extends ScalaCodegen
{

    val IR: BaseExp with FunctionsExp



    @transient var compiler: Global = _
    @transient var reporter: ConsoleReporter = _

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
    var silent = false

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
            case List (mA, mB) =>
				val mAUnsafe = mA.asInstanceOf[Manifest[A]]
				val mBUnsafe = mB.asInstanceOf[Manifest[B]]
				compileFunction (
					IR.doApply (f, _: IR.Rep[A])(
						mAUnsafe,
						mBUnsafe,
						if (f.pos.nonEmpty)
							f.pos.head
						else
							null
					)
				)(mAUnsafe, mBUnsafe)
		}
    }

	def functionToScalaCodeWithDynamicManifests[A, B](f: IR.Rep[A => B]) : ClassCode[A,B] = {
		val List (mA, mB) = f.tp.typeArguments
		val mAUnsafe = mA.asInstanceOf[Manifest[A]]
		val mBUnsafe = mB.asInstanceOf[Manifest[B]]
		functionToScalaCode (
			IR.doApply (f, _: IR.Rep[A])(
				mAUnsafe,
				mBUnsafe,
				if (f.pos.nonEmpty)
					f.pos.head
				else
					null
			)
		)(mAUnsafe, mBUnsafe)
	}

    def functionToScalaCode[A: Manifest, B: Manifest](f: IR.Rep[A] => IR.Rep[B]) : ClassCode[A,B] = {
        if (this.compiler eq null)
            setupCompiler ()

        val className = "staged$" + compileCount
        compileCount += 1

        val source = new StringWriter ()
        val staticData = emitSource (f, className, new PrintWriter (source))
        // IR.reset

        if (dumpGeneratedCode) println (source)

        ClassCode(className, source.toString)
    }

    def compileScalaCode[A, B](code : ClassCode[A,B]) : A => B = {
		if (this.compiler eq null)
			setupCompiler ()

        val compiler = this.compiler
        val run = new compiler.Run

        val fileSystem = new VirtualDirectory ("<vfs>", None)
        compiler.settings.outputDirs.setSingleOutput (fileSystem)

        run.compileSources (List (new scala.reflect.internal.util.BatchSourceFile ("<stdin>", code.source.toString)))


        if (!silent) {
            reporter.printSummary ()
            if (!reporter.hasErrors)
                println ("compilation: ok")
            else
                println ("compilation: had errors")
        }

        reporter.reset ()

        val loader = new AbstractFileClassLoader (fileSystem, this.getClass.getClassLoader)

        val cls: Class[_] = loader.loadClass (code.className)

		//TODO: Reimplement static data
        val cons = cls.getConstructor() //(staticData.map (_._1.tp.runtimeClass): _*)
        val obj: A => B = cons.newInstance().asInstanceOf[A => B] //(staticData.map (_._2.asInstanceOf[AnyRef]): _*).asInstanceOf[A => B]
        obj
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


    import IR.Sym
    override def emitSource[A : Manifest](args: List[Sym[_]], body: Block[A], className: String, out: PrintWriter) = {

        val sA = remap(manifest[A])

        val staticData = getFreeDataBlock(body)

        withStream(out) {
            stream.println("/*****************************************\n"+
                "  Emitting Generated Code                  \n"+
                "*******************************************/")
            emitFileHeader()

            //Normal version
            stream.println("class "+className+(if (staticData.isEmpty) "" else "("+staticData.map(p=>"p"+quote(p._1)+":"+p._1.tp).mkString(",")+")")
                   + " extends (("+args.map(a => remap(a.tp)).mkString(", ") + ")=>(" + sA + ")) {")

            //Serializeable version
//            stream.println(s"@SerialVersionUID(${java.util.UUID.randomUUID().getLeastSignificantBits}L)")
//            stream.println("class "+className+(if (staticData.isEmpty) "" else "("+staticData.map(p=>"p"+quote(p._1)+":"+p._1.tp).mkString(",")+")")
//                + " extends (("+args.map(a => remap(a.tp)).mkString(", ") + ")=>(" + sA + ")) with Serializable {")

            //Case class version
//            stream.println("case class "+className+(if (staticData.isEmpty) "()" else "("+staticData.map(p=>"p"+quote(p._1)+":"+p._1.tp).mkString(",")+")")
//                            + " extends (("+args.map(a => remap(a.tp)).mkString(", ") + ")=>(" + sA + ")) {")



            stream.println("def apply("+args.map(a => quote(a) + ":" + remap(a.tp)).mkString(", ")+"): "+sA+" = {")

            emitBlock(body)
            stream.println(quote(getBlockResult(body)))

            stream.println("}")

            stream.println("}")
            stream.println("/*****************************************\n"+
                "  End of Generated Code                  \n"+
                "*******************************************/")
        }

        staticData
    }

}
