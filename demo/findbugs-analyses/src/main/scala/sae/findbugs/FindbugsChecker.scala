package sae.findbugs

import analyses.{SE_NO_SUITABLE_CONSTRUCTOR, IMSE_DONT_CATCH_IMSE, DM_GC}
import sae.collections.QueryResult
import sae.bytecode.model.{ExceptionHandler, MethodReference}
import sae.bytecode.{MaterializedDatabase, BytecodeDatabase}
import java.io.FileInputStream
import sae.profiler.Profiler._
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.dependencies.Dependency

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 12:29
 *
 */
object FindbugsChecker
{

    private val usage =
        ("""FindbugsChecker [<jarFile> ...]
                |<jarFile>: The path to a jar file containong code to be checked
                """).stripMargin

    def main(args: Array[String]) {

        if (args.length == 0 || !args.forall(arg ⇒ arg.endsWith(".zip") || arg.endsWith(".jar"))) {
            println(usage)
            sys.exit(1)
        }

        for (arg ← args) {
            val file = new java.io.File(arg)
            if (!file.canRead || file.isDirectory) {
                println("The file: " + file + " cannot be read.");
                println(usage)
                sys.exit(1)
            }
        }

        val database = new BytecodeDatabase
        val materializedDatabase = new MaterializedDatabase(database)

        println("Reading class files:")
        var readingTime: Long = 0
        var databaseFillers: List[() => Unit] = Nil
        for (jar <- args) {
            val fillDatabase = profile(time => (readingTime += time))(readClassfiles(database, jar))
            databaseFillers = fillDatabase :: databaseFillers
        }
        println("Took: " + nanoToSeconds(readingTime))


        println("Filling datbase:")
        var fillingTime: Long = 0
        for (filler <- databaseFillers) {
            profile(time => fillingTime += time)(filler.apply())
        }
        println("Took: " + nanoToSeconds(fillingTime))

        println("Number of class files: " + materializedDatabase.declared_types.size)

        analyzeFromMaterialized(materializedDatabase)

        sys.exit(0)
    }


    def readClassfiles(database: BytecodeDatabase, jar: String): () => Unit = {
        val stream = new FileInputStream(jar)
        val transformer = database.transformerForArchiveStream(stream)
        stream.close()
        () => transformer.processAllFacts
    }

    def analyzeFromMaterialized(database: MaterializedDatabase) {
        import sae.collections.Conversions._

        val garbageCollectionInvocations: QueryResult[Dependency[MethodReference, MethodReference]] = DM_GC(database)
        profile(time => println("DM_GC: " + nanoToSeconds(time)))(
            garbageCollectionInvocations.lazyInitialize()
        )
        println("# Violations: " + garbageCollectionInvocations.size)
        //garbageCollectionInvocations.foreach(println)

        val catchesIllegalMonitorStateException: QueryResult[ExceptionHandler] = IMSE_DONT_CATCH_IMSE(database)
        profile(time => println("IMSE_DONT_CATCH_IMSE: " + nanoToSeconds(time)))(
            catchesIllegalMonitorStateException.lazyInitialize()
        )
        println("# Violations: " + catchesIllegalMonitorStateException.size)

        val serializableClassWithoutDefaultConstructorInSuperClass: QueryResult[ObjectType] = SE_NO_SUITABLE_CONSTRUCTOR(database)
        profile(time => println("SE_NO_SUITABLE_CONSTRUCTOR: " + nanoToSeconds(time)))(
            serializableClassWithoutDefaultConstructorInSuperClass.lazyInitialize()
        )
        println("# Violations: " + serializableClassWithoutDefaultConstructorInSuperClass.size)
        //serializableClassWithoutDefaultConstructorInSuperClass.foreach(println)
    }


}