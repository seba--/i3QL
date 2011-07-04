package sae

import bytecode.BytecodeDatabase
import profiler.util.DataQueryAnalyzer
import java.io.{PrintWriter, FileOutputStream, OutputStream}
import util.JarUtil._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 29.06.11 09:35
 *
 * A command line tool for generating TikZ plots of query throughput
 */
object QueryDataTikZPlot {

    val synopsis =  """QueryDataTikZPlot jarFiles|directory [useClasspath outputFile]
                    |% prints a TikZ plot of all derived database queries and their respective throughput
                    |% mandatory parameters
                    |jarFile:      comma separated list of jar files
                    |directory:    directory to search for jar files
                    |% optional parameters
                    |% (if one parameter is present all previous optinal paremeters are assumed to be present)
                    |useClasspath: (true|false) use the classpath to search for the jar files
                    |outputFile:   a file where generated tex sources are saved
                    """.stripMargin

    def main(args: Array[String])
    {
        if (args.size < 1) {
            println(synopsis)
            return
        }

        val useClasspath = (args.size == 2 && (args(1) == "true"))

        var out: OutputStream = System.out
        if (args.size == 3) {
            out = new FileOutputStream(args(2))
        }

        val db = new BytecodeDatabase
        println("setting up queries")
        val profile = createProfile( createQueries(db) )

        val jarFiles = args(0).split(",")

        println("pushing data to database")
        val urls =
            if( !useClasspath)
            {
                resolveDirectoryAndJarUrisFromFilesystem(jarFiles)
            }
            else
            {
                resolveDirectoryAndJarUrisFromClasspath(jarFiles)
            }
        db.transformerForArchiveStreams(urls.filter(_.getFile.endsWith("jar")).map(_.openStream)).processAllFacts()


        val writer = new PrintWriter(out, true)
        writer.println( profile.toTikZ )

        writer.close()
    }


    def createQueries( db : BytecodeDatabase) : List[LazyView[_]] = db.derivedViews

    def createProfile(queries : List[LazyView[_]]) =
    {
        val analyzer = new DataQueryAnalyzer

        queries.foreach( (view : LazyView[_]) => analyzer(view.asInstanceOf[LazyView[AnyRef]]) ) // TODO this is not nice

        analyzer.profile
    }
}