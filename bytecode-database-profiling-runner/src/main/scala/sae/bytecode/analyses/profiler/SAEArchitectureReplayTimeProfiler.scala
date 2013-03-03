/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.bytecode.analyses.profiler


import sae.analyses.architecture.hibernate.HibernateEnsembles.{Hibernate_3_6_6, Hibernate_3_0}
import sae.analyses.architecture.hibernate.HibernateSlices
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.profiler.statistics._
import sae.bytecode.profiler.util.MilliSeconds
import sae.bytecode.profiler.TimeMeasurement
import java.io.{FileWriter, PrintWriter, FileInputStream, File}
import unisson.model.UnissonDatabase
import collection.JavaConversions
import collection.mutable
import de.tud.cs.st.vespucci.interfaces.{IConstraint, IEnsemble}


/**
 *
 * @author Ralf Mitschke
 *
 */

object SAEArchitectureReplayTimeProfiler
    extends TimeMeasurement
{

    def reReadJars: Boolean = isReReadJars

    private var isReReadJars = false


    def main(args: Array[String]) {
        if (args.length == 0 || !args (0).endsWith (".properties")) {
            println (usage)
            sys.exit (1)
        }

        val propertiesFile = args (0)

        isReReadJars =
            if (args.length > 1) {
                java.lang.Boolean.parseBoolean (args (1))
            }
            else
            {
                true
            }

        val properties = getProperties (propertiesFile).getOrElse (
        {
            println ("could not find properties file or resource: " + propertiesFile)
            sys.exit (-1)
        }
        )

        val warmupIterations = properties.getProperty ("sae.warmup.iterations").toInt
        val warmupLocation = properties.getProperty ("sae.warmup.location")

        val measurementIterations = properties.getProperty ("sae.benchmark.iterations").toInt
        val measurementLocation = properties.getProperty ("sae.benchmark.location")

        val queries =
            if (properties.getProperty ("sae.benchmark.queries").isEmpty)
            {
                Nil
            }
            else
            {
                properties.getProperty ("sae.benchmark.queries").split (";").toList
            }

        val outputFile = properties.getProperty ("sae.benchmark.out", System.getProperty ("user.dir") + "/bench.txt")


        println ("Warmup: " + warmupIterations + " times : " + queries + " on " + warmupLocation + " re-read = " + reReadJars)

        val counts = warmup (warmupIterations, warmupLocation, queries, reReadJars)

        println ("\tdone")
        println ("Num. of Results: " + counts)


        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()

        println ("Measure: " + measurementIterations + " times : " + queries + " on " + measurementLocation + " re-read = " + reReadJars)
        val statistics = measure (measurementIterations, measurementLocation, queries, reReadJars)
        println ("\tdone")

        //println (statistics.summary (measurementUnit))


        //println (dataStatistics.summary)

        reportCSV (outputFile, warmupIterations, measurementIterations, measurementLocation, queries, statistics)

        sys.exit (0)
    }


    def reportCSV(outputFile: String, warmUpIterations: Int, measurementIterations: Int, measurementLocation: String, queries: List[String],
                  statistics: List[SampleStatistic])
    {
        val file = new File (outputFile)
        val writeHeader = !file.exists ()

        val out = new PrintWriter (new FileWriter (file, true))

        val separator = ";"

        val header = "bench type" + separator + "location" + separator + "timestamp" + separator + "change size" + separator +
            "num. classes" + separator + "num. methods" + separator + "num. fields" + separator + "num. instructions" + separator +
            "num. warmup iterations" + separator + "num. measure iterations" + separator +
            "re-read jars" + separator + "optimized" + separator + "transactional" + separator + "shared" + separator +
            "queries" + separator + "newResult count" + separator + "mean" + separator + "std. dev" + separator + "std err." + separator + "measured unit"




        if (writeHeader) {
            out.println (header)
        }

        var i = 0
        while (i < statistics.size) {
            val outputLine =
                "ArchReplay" + separator +
                    measurementLocation + separator +
                    warmUpIterations + separator +
                    measurementIterations + separator +
                    reReadJars.toString + separator +
                    (if (queries.isEmpty) {
                        "NONE" + separator
                    }
                     else
                     {
                         queries.reduce (_ + " | " + _) + separator
                     }) +
                    ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit
                        .fromBase (statistics (i).mean))) + separator +
                    ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit
                        .fromBase (statistics (i).standardDeviation))) + separator +
                    ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit
                        .fromBase (statistics (i).standardError))) + separator +
                    measurementUnit.descriptor

            out.println (outputLine)
            i += 1
        }
        out.close ()
    }


    def isFile(propertiesFile: String): Boolean = {
        val file = new java.io.File (propertiesFile)
        file.exists () && file.canRead && !file.isDirectory

    }

    def isResource(propertiesFile: String): Boolean = {
        this.getClass.getClassLoader.getResource (propertiesFile) != null
    }

    def getProperties(propertiesFile: String): Option[java.util.Properties] = {
        if (isFile (propertiesFile)) {
            val file = new java.io.File (propertiesFile)
            val properties = new java.util.Properties ()
            properties.load (new FileInputStream (file))
            return Some (properties)
        }
        if (isResource (propertiesFile)) {
            val properties = new java.util.Properties ()
            properties.load (this.getClass.getClassLoader.getResource (propertiesFile).openStream ())
            return Some (properties)
        }
        None
    }

    def measurementUnit = MilliSeconds

    val usage: String = """|Usage: java SAEAnalysesReplayTimeProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin

    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, jar: String, queries: List[String], includeReadTime: Boolean): List[SampleStatistic] = {

        val size = applyAnalysesWithJarReading (jar: String, queries: List[String]).size

        if (includeReadTime) {
            measureTime (iterations, size)(() => applyAnalysesWithJarReading (jar, queries))
        }
        else
        {
            //applyAnalysesWithoutJarReading (materializedDatabase.get, queries)
            throw new UnsupportedOperationException ("replay without class file time not supported")
        }
    }

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, jar: String, queries: List[String], includeReadTime: Boolean): List[Long] = {

        var i = 0
        while (i < iterations) {
            if (includeReadTime) {
                applyAnalysesWithJarReading (jar, queries)
            }
            else
            {
                //applyAnalysesWithoutJarReading (materializedDatabase.get, queries)
                throw new UnsupportedOperationException ("replay without class file time not supported")
            }
            println ()
            i += 1
        }

        Nil
    }


    /**
     * performs the measurement of function f, iterations times.
     * f should return the time taken to perform the required computation.
     * A statistic is returned for the time consumed when applying f
     */
    def measureTime(iterations: Int, sampleSize: Int)(f: () => List[Long]): List[SampleStatistic] = {
        val statistics = Array.fill (sampleSize)(Statistic (iterations))

        for (i <- 1 to iterations)
        {
            val results = f ()
            var j = 0
            while (j < sampleSize) {
                statistics (j).add (results (j))
                j += 1
            }
            println ()
        }
        statistics.toList
    }

    private def applyAnalysesWithJarReading(jar: String, queries: List[String]): List[Long] = {
        import JavaConversions._
        val database = BATDatabaseFactory.create ()
        //val results = queries.foreach (q => getAnalysis (q, database))
        val unisson = new UnissonDatabase (database)

        unisson.addEnsembles (Hibernate_3_0.getEnsembles ())
        for ((slice, ensembles) <- HibernateSlices.Hibernate_3_0.ensembles) {
            unisson.addEnsemblesToSlice (ensembles)(slice)
        }

        for ((slice, constraints) <- HibernateSlices.Hibernate_3_0.constraints) {
            unisson.addConstraintsToSlice (constraints)(slice)
        }
        val results = unisson.violations

        val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
        database.addArchive (stream)
        stream.close ()


        val oldEnsembles: mutable.Set[IEnsemble] = Hibernate_3_0.getEnsembles ()
        val newEnsembles: mutable.Set[IEnsemble] = Hibernate_3_6_6.getEnsembles ()

        val oldConstraints: mutable.Map[String, mutable.HashSet[IConstraint]] = HibernateSlices.Hibernate_3_0.constraints
        val newConstraints: mutable.Map[String, mutable.HashSet[IConstraint]] = HibernateSlices.Hibernate_3_6_6.constraints



        val ensembleUpdates = for (oldE <- oldEnsembles; newE <- newEnsembles; if oldE.getName == newE.getName) yield (oldE, newE)

        val addedEnsembles = for (newE <- newEnsembles; if !oldEnsembles.exists (oldE => oldE.getName == newE.getName)) yield (newE)

        /*
        val updatedConstraints = for {
            (oldS, oldCL) <- oldConstraints
            (newS, newCL) <- newConstraints
            if oldS == newS
            oldC <- oldCL
            newC <- newCL
            if (oldC != newC)
        } yield (oldC, newC)
        */

        val addedConstraints = for {
            (oldS, oldCL) <- oldConstraints
            (newS, newCL) <- newConstraints
            if oldS == newS
            newC <- newCL
            if (!oldCL.exists (_ == newC))
        } yield (newC)


        val removedConstraints = for {
            (oldS, oldCL) <- oldConstraints
            (newS, newCL) <- newConstraints
            if oldS == newS
            oldC <- oldCL
            if (!newCL.exists (_ == oldC))
        } yield (oldC)

        val ensembleUpdateTimes =
            for ((oldE, newE) <- ensembleUpdates) yield
            {
                println ("update: " + oldE.getName)
                var taken: Long = 0
                time {
                    l => taken += l
                }
                {
                    unisson.updateEnsemble (oldE, newE)
                }

                taken
            }

        val ensembleAdditionTimes =
            for (newE <- addedEnsembles) yield
            {
                println ("adding: " + newE.getName)
                var taken: Long = 0
                time {
                    l => taken += l
                }
                {
                    unisson.addEnsemble (newE)
                }

                taken
            }

        val constraintAdditionTimes =
            for (newC <- addedConstraints) yield
            {
                println ("adding: " + newC)
                var taken: Long = 0
                time {
                    l => taken += l
                }
                {
                    unisson.addConstraintToSlice(newC)
                }

                taken
            }

        val constraintDeletionTimes =
            for (oldC <- removedConstraints) yield
            {
                println ("removing: " + oldC)
                var taken: Long = 0
                time {
                    l => taken += l
                }
                {
                    unisson.removeConstraintFromSlice(oldC)
                }

                taken
            }


        (ensembleUpdateTimes ++ ensembleAdditionTimes ++ constraintAdditionTimes ++ constraintDeletionTimes).toList
    }


}
