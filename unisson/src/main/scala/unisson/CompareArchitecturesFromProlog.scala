package unisson

import prolog.parser.UnissonPrologParser
import java.io._
import queries.QueryCompiler
import unisson.ast._
import Utilities._
import sae.bytecode.BytecodeDatabase

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 09:53
 *
 */

object CompareArchitecturesFromProlog
{

    private val ensembles = "--ensembles"

    private val constraints = "--constraints"

    private val prefixChange = "--prefixChange"

    private val outputOption = "--out"

    private val usage = ("""CompareArchitecturesFromProlog <sadFiles1> <codeLocations1> <sadFiles2> <codeLocations2>
                |CheckArchitectureFromProlog  [""" + outputOption + """ <csvFile>]
                |<sadFiles>: A sad file architecture definition. Multiple sad files can be given as " " separated list. Implicitly a .sad.pl file assumed to be present for each .sad file
                |<codeLocations>: A code location may be one of the following:
                |                 - a jar file
                |                 - .class file
                |                 Multiple code locations can be given as " " separated list.
                |<csvFile>      : A comma separated value file where output is written to
                |""" + ensembles + """ : compares the ensembles in the two sad files
                |""" + constraints + """ : compares the constraints in the two sad files
                |""" + prefixChange + """ <String>: indicates that all packages had their prefixed changed, thus queries will be compared based on the changed prefixes.
                """).stripMargin
    //TODO make directories a code location
    //                |                - a directory, which is searched recursively for .class files

    def main(args: Array[String])
    {
        if (args.length < 4) {
            println(usage)
            return
        }
        val sadFiles1 = args(0).split(" ")

        val codeLocs1 = args(1).split(" ")

        val sadFiles2 = args(2).split(" ")

        val codeLocs2 = args(3).split(" ")


        val trail = args.drop(4)

        var output = ""

        var prefix : Array[String] = Array("","")

        var printEnsembles = false

        var printConstraints = false

        var i = 0
        var consumeNext = false
        trail.foreach(
                (s: String) => {
                s match {
                    case _ if s == ensembles => printEnsembles = true
                    case _ if s == constraints => printConstraints = true
                    case _ if s == prefixChange => {
                        if (i + 1 <= trail.size - 1) {
                            prefix = trail(i + 1).split("=")
                            consumeNext = true
                        }
                        else {
                            println(prefixChange + " specified without a value")
                            System.exit(-1)
                        }
                    }
                    case _ if s == outputOption => {
                        if (i + 1 <= trail.size - 1) {
                            output = trail(i + 1)
                            consumeNext = true
                        }
                        else {
                            println(outputOption + " specified without a value")
                            System.exit(-1)
                        }
                    }
                    case _ if (consumeNext) => consumeNext = false // do nothing
                    case _ if (!consumeNext) => {
                        println("Unknown option: " + s)
                        System.exit(-1)
                    }
                }


                i = i + 1
            }
        )

        implicit val outputWriter = if (output == "") {
            System.out
        } else {
            new PrintStream(new FileOutputStream(output), true)
        }

        val checker1 = readArchitectures(sadFiles1, codeLocs1)
        val checker2 = readArchitectures(sadFiles2, codeLocs2)

        implicit val delimiter = ";"


        if (printEnsembles) {

            outputWriter.println("Ensemble" + delimiter + "Architecture 1 Count" + delimiter + "Architecture 2 Count" + delimiter + "Type of Change (added|removed|renamed|query|hierarchy)")

            val subst = (prefix(0), prefix(1))
            val ensemblesOnlyIn1 = checker1.getEnsembles.filter( (e:Ensemble) => checker2.getEnsemble(Utilities.substitutePrefix(e.name)(subst)) == None )
            val ensemblesOnlyIn2 = checker2.getEnsembles.filter( (e:Ensemble) =>
                checker1.getEnsemble(Utilities.substitutePrefix(e.name)((prefix(1), prefix(0)))) == None
            )
            val ensemblesInBoth = checker1.getEnsembles.collect( (e1:Ensemble) =>
                checker2.getEnsemble(Utilities.substitutePrefix(e1.name)(subst)) match
                {
                    case Some(e2 @ Ensemble(_,_,_,_)) => (e1, e2)
                }
            )

            ensemblesOnlyIn1.foreach( (e:Ensemble) =>
                {
                    outputWriter.println(e.name + delimiter + checker1.ensembleElements(e).size + delimiter + "N/A" + delimiter + "removed")
                }
            )

            ensemblesOnlyIn2.foreach( (e:Ensemble) =>
                {
                    outputWriter.println(e.name + delimiter + "N/A" + delimiter + checker2.ensembleElements(e).size + delimiter + "added")
                }
            )

            ensemblesInBoth.foreach{ case (e1, e2) =>
                {
                    val query1 = UnissonQuery.asString(e1.query)( (prefix(0), prefix(1)) )
                    val query2 = UnissonQuery.asString(e2.query)

                    val queryChanged = query1 != query2

                    if(queryChanged)
                    {
                        val deltaIndex = compareStrings(query1, query2)
                        val delta1 = query1.substring(deltaIndex, query1.length() )
                        val delta2 = query2.substring(deltaIndex, query2.length() )
                        val common = query1.substring(0, deltaIndex)
                        outputWriter.println(e1.name + delimiter + checker1.ensembleElements(e1).size + delimiter + checker2.ensembleElements(e2).size + delimiter + "query" + delimiter + common + delimiter + delta1 + delimiter + delta2)
                    }
                    else
                    {
                        val nameChanged = e1.name != e2.name
                        if(nameChanged)
                        {
outputWriter.println(e1.name + delimiter + checker1.ensembleElements(e1).size + delimiter + checker2.ensembleElements(e2).size + delimiter + "renamed"  + delimiter + e1.name + delimiter + e2.name )
                        }
                    }
                }
            }
        }

        if (printConstraints) {


        }


    }




    /**
     * returns the smallest index where the two strings are equal
     */
    def compareStrings(thisString:String, anotherString: String): Int =
    {
        val len1: Int = thisString.length()
        val len2: Int = anotherString.length()
        val n: Int = scala.math.min(len1, len2)
        val v1: Array[Char] = thisString.toCharArray
        val v2: Array[Char] = anotherString.toCharArray
        var i: Int = 0
        var j: Int = 0
        if (i == j) {
            var k: Int = i
            while (k < n) {
                val c1: Char = v1(k)
                val c2: Char = v2(k)
                if (c1 != c2) {
                    return k
                }
                k += 1;
            }
        }
        n
    }

    def readArchitectures(sadFiles: Array[String], codeLocations: Array[String]): ArchitectureChecker =
    {
        val database = new BytecodeDatabase
        val checker = new ArchitectureChecker(database)
        val compiler = new QueryCompiler(checker)

        sadFiles.foreach(
                (sadFile: String) => {
                val plFile = sadFile + ".pl"
                println("reading architecture from " + plFile)
                compiler.addAll(
                    readSadFile(
                        fileNameAsStream(plFile)
                    ).collect{ case e @ Ensemble(_,_,_,_) => e} // only read ensembles we do not want to evaluate the violations
                )
            }
        )
        compiler.finishOutgoing()

        val classPattern = """.*\.class""".r

        val jarPattern = """.*\.jar""".r

        codeLocations.map(
                (loc: String) => loc match {
                case classPattern() => {
                    println("reading bytecode from " + loc)
                    database.transformerForClassfileStream(fileNameAsStream(loc)).processAllFacts()
                }
                case jarPattern() => {
                    println("reading bytecode from " + loc)
                    database.transformerForArchiveStream(fileNameAsStream(loc)).processAllFacts()
                }
                case _ => println("unrecognized code location type : " + loc)
            }
        )

        checker
    }


}