package unisson

import java.io._
import unisson.ast._
import Utilities._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 09:53
 *
 */

object CheckArchitectureFromProlog
{


    private val sadListOption = "--sad"

    private val jarListOption = "--code"

    private val outputOption = "--out"

    private val duplicates = "--duplicates"

    private val ensembles = "--ensembles"

    private val constraints = "--constraints"

    private val violations = "--violations"

    private val showRest = "--rest"

    private val usage = ("""CheckArchitectureFromProlog [<sadFile> <codeLocation>]
                |CheckArchitectureFromProlog [""" + sadListOption + """ [<sadFileList>] | <sadFile>] [""" + jarListOption + """ [<codeLocationList>] | <codeLocation>] [""" + outputOption + """ <csvFile>] [""" + violations + """] [""" + duplicates + """] [""" + ensembles + """]
                |<sadFile>: A sad file architecture definition. Implicitly a .sad.pl file assumed to be present
                |<codeLocation>: A code location may be one of the following:
                |                - a jar file
                |                - .class file
                |<sadFileList> : A whitespace separated list of sad files
                |<jarFileList> : A whitespace separated list of jar files
                |<csvFile>     : A comma separated value file where output is written to
                |""" + violations + """ : outputs all violations
                |""" + duplicates + """ : outputs all elements that belong to two or more ensembles simultaniously (along with the respective ensembles).
                |""" + ensembles + """ : outputs all ensembles with a count of contained elements
                |""" + constraints + """ : outputs all constraints with counts for constraint violations
                |""" + showRest + """ : outputs all elements that are not contained in an ensemble
                """).stripMargin
    //TODO make directories a code location
    //                |                - a directory, which is searched recursively for .class files

    def main(args: Array[String])
    {
        if (args.length == 0) {
            println(usage)
            return
        }
        var sadFiles: Array[String] = Array()

        var codeLocations: Array[String] = Array()

        if (args(0) == sadListOption) {
            sadFiles = args.dropRight(1).drop(1).takeWhile(
                    (s: String) =>
                    s != jarListOption && s != violations && s != outputOption && s != duplicates && s != ensembles
            )

        }
        else {
            sadFiles = Array(args.head)
        }

        val rest = args.drop(sadFiles.size)

        if (rest.length == 0) {
            println("Not enough arguments -- specified " + sadFiles.size + "sad files and no code locations")
        }



        if (rest(0) == jarListOption) {
            codeLocations = rest.drop(1).takeWhile(
                    (s: String) =>
                    s != jarListOption && s != violations && s != outputOption && s != duplicates && s != ensembles
            )
        }
        else {
            codeLocations = Array(rest.head)
        }

        val trail = rest.drop(codeLocations.size)

        var printViolations = false
        var printDuplicates = false
        var printEnsembles = false
        var printRest = false
        var printConstraints = false
        var output = ""

        var i = 0
        var consumeNext = false
        trail.foreach(
                (s: String) => {
                s match {
                    case _ if s == violations => printViolations = true
                    case _ if s == duplicates => printDuplicates = true
                    case _ if s == ensembles => printEnsembles = true
                    case _ if s == constraints => printConstraints = true
                    case _ if s == showRest => printRest = true
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

        implicit val checker = checkArchitectures(sadFiles, codeLocations)

        implicit val delimiter = ";"


        if (printRest) {

            outputWriter.println("Type" + delimiter + "Element")

            checker.ensembleElements(checker.getEnsemble("@rest").get).foreach(
                    (e: SourceElement[AnyRef]) => outputWriter.println(elementToString(e))
            )
        }


        if (printEnsembles) {

            outputWriter.println("Ensemble" + delimiter + "EnsembleElementCount")

            (checker.getEnsembles.toList.sortBy {
                _.name
            }).foreach((e: Ensemble) => outputWriter.println(ensembleToString(e)))
        }

        if (printConstraints) {

            outputWriter.println(
                "Type" + delimiter + "Kind" + delimiter + "Source Ensembles(s)" + delimiter + "Target Ensembles(s)" + delimiter + "Violation Count"
            )
            (
                    checker.getConstraints.toList.sortBy {
                            (c: DependencyConstraint) =>
                            (c.sources.map(_.name).reduce(_ + _), c.targets.map(_.name).reduce(_ + _))
                    }
                    ).foreach((c: DependencyConstraint) => outputWriter.println(constraintToString(c)))
        }

        if (printViolations) {
            checker.violations.foreach((v: Violation) => outputWriter.println(violationToString(v)))
        }


        if (printDuplicates) {
            var pairs: List[(Ensemble, Ensemble)] = Nil
            for (
                first <- checker.getEnsembles.filter((e: Ensemble) => !e.name.startsWith("@"));
                second <- checker.getEnsembles.filter(
                        (e: Ensemble) =>
                        e != first &&
                                !first.allDescendents.contains(e) &&
                                !first.allAncestors.contains(e) &&
                                !e.name.startsWith("@")
                )
            ) {
                if (!pairs.contains((first, second)) && !pairs.contains((second, first))) {
                    pairs = pairs :+ (first, second)
                }
            }

            val double = pairs.flatMap {
                case (fst, snd) => {
                    // println("checking pair (" + fst.name + ", " + snd.name + ")")
                    val elemA = checker.ensembleElements(fst).asList
                    val elemB = checker.ensembleElements(snd).asList
                    val doubles = elemA.filter(elemB.contains(_))
                    for (elem <- doubles) yield (fst, snd, elem)
                }
            }
            double.foreach {
                case (fst, snd, elem) => outputWriter.println(
                    fst.name + delimiter + snd.name + delimiter + elementToString(
                        elem
                    )
                )
            }

        }

    }

}