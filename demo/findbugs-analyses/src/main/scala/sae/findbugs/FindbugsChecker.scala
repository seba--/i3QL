package sae.findbugs

import analyses._
import sae.collections.QueryResult
import sae.bytecode.{MaterializedDatabase, BytecodeDatabase}
import java.io.FileInputStream
import sae.profiler.Profiler._
import sae.bytecode.model.dependencies.Dependency
import de.tud.cs.st.bat.{ReferenceType, ObjectType}
import sae.bytecode.model._

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

        val protectedFieldsInFinalClasses: QueryResult[(ClassDeclaration, FieldDeclaration)] = CI_CONFUSED_INHERITANCE(database)
        profile(time => println("CI_CONFUSED_INHERITANCE: " + nanoToSeconds(time)))(
            protectedFieldsInFinalClasses.lazyInitialize()
        )
        println("# Violations: " + protectedFieldsInFinalClasses.size)
        //protectedFieldsInFinalClasses.foreach(println)

        val garbageCollectionInvocations: QueryResult[Dependency[MethodDeclaration, MethodReference]] = DM_GC(database)
        profile(time => println("DM_GC: " + nanoToSeconds(time)))(
            garbageCollectionInvocations.lazyInitialize()
        )
        println("# Violations: " + garbageCollectionInvocations.size)
        //garbageCollectionInvocations.foreach(println)

        val classesWithPublicFinalizeMethods: QueryResult[ReferenceType] = FI_PUBLIC_SHOULD_BE_PROTECTED(database)
        profile(time => println("FI_PUBLIC_SHOULD_BE_PROTECTED: " + nanoToSeconds(time)))(
            classesWithPublicFinalizeMethods.lazyInitialize()
        )
        println("# Violations: " + classesWithPublicFinalizeMethods.size)
        //classesWithPublicFinalizeMethods.foreach(println)

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

        val unusedPrivateFields: QueryResult[FieldDeclaration] = UUF_UNUSED_FIELD(database)
        profile(time => println("UUF_UNUSED_FIELD: " + nanoToSeconds(time)))(
            unusedPrivateFields.lazyInitialize()
        )
        println("# Violations: " + unusedPrivateFields.size)

        import sae.syntax.RelationalAlgebraSyntax._
        val violatingClasses: QueryResult[ObjectType]  = δ(Π( (f:FieldDeclaration) => f.declaringClass)(unusedPrivateFields))
        println("# Violating Classes (for comparison to BAT): " + violatingClasses.size)
        //unusedPrivateFields.foreach( fd => println(fd.declaringClass + "." + fd.name))

    }

}