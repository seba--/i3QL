package sae

import bytecode.BytecodeDatabase
import profiler.util.DataQueryAnalyzer
import java.io.{PrintWriter, FileOutputStream, OutputStream}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 29.06.11 09:35
 *
 * A command line tool for generating TikZ plots of query throughput
 */
object QueryDataTikZPlot {

    val synopsis =  """QueryDataTikZPlot jarFiles [outputFile]
                    |% prints a TikZ plot of all derived database queries and their respective throughput
                    |% mandatory parameters
                    |jarFile:    comma separated list of jar files
                    |% optional parameters:
                    |outputFile: a file where generated tex sources are saved
                    """.stripMargin

    def main(args: Array[String])
    {
        if (args.size < 1) {
            println(synopsis)
            return
        }

        var out: OutputStream = System.out
        if (args.size == 2) {
            out = new FileOutputStream(args(1))
        }

        val db = new BytecodeDatabase
        print("setting up queries")
        val profile = createProfile( createQueries(db) )

        val jarFiles = args(0).split(",")

        print("pushing data to database")
        for( jar <- jarFiles ) db.addArchiveAsFile(jar)

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