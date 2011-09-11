package unisson

import prolog.parser.UnissonPrologParser
import java.io._
import java.lang.IllegalArgumentException
import prolog.utils.ISOPrologStringConversion
import unisson.ast._
import sae.bytecode.BytecodeDatabase
import unisson.queries.QueryCompiler
import Utilities._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 09:53
 *
 */

object CheckArchitectureFromProlog
{


    private val ensembleFunctor = "ensemble"

    private val dependencyConstraintFunctors = List("incoming", "outgoing", "not_allowed", "inAndOut", "expected")

    private val parser = new UnissonPrologParser()

    private val sadListOption = "--sad"

    private val jarListOption = "--code"

    private val outputOption = "--out"

    private val disjunct = "--disjunct"

    private val overview = "--overview"

    private val violations = "--violations"

    private val classPattern = """.*\.class""".r

    private val jarPattern = """.*\.jar""".r

    private val usage = ("""CheckArchitectureFromProlog [<sadFile> <codeLocation>]
                |CheckArchitectureFromProlog [""" + sadListOption + """ [<sadFileList>] | <sadFile>] [""" + jarListOption + """ [<codeLocationList>] | <codeLocation>] [""" + outputOption + """ <csvFile>] [""" + violations + """] [""" + disjunct + """] [""" + overview + """]
                |<sadFile>: A sad file architecture definition. Implicitly a .sad.pl file assumed to be present
                |<codeLocation>: A code location may be one of the following:
                |                - a jar file
                |                - .class file
                |<sadFileList> : A whitespace separated list of sad files
                |<jarFileList> : A whitespace separated list of jar files
                |<csvFile>     : A comma separated value file where output is written to
                |""" + violations + """ : outputs all violations
                |""" + disjunct + """ : outputs all elements that belong to two or more ensembles simultaniously (along with the respective ensembles).
                |""" + overview + """ : outputs all ensembles, their constraints, as well as counts for ensemble elements and constraint violations
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
                    s != jarListOption && s != violations && s != outputOption && s != disjunct && s != overview
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
                    s != jarListOption && s != violations && s != outputOption && s != disjunct && s != overview
            )
        }
        else {
            codeLocations = Array(rest.head)
        }

        val trail = rest.drop(codeLocations.size)

        var printViolations = false
        var printDisjunct = false
        var printOverview = false
        var output = ""

        var i = 0
        var consumeNext = false
        trail.foreach(
                (s: String) => { s match
                {
                    case  _ if s == violations => printViolations = true
                    case  _ if s == disjunct => printDisjunct = true
                    case  _ if s == overview => printOverview = true
                    case  _ if s == outputOption => {
                        if( i + 1 <= trail.size - 1)
                        {
                            output = trail(i+1)
                            consumeNext = true
                        }
                        else
                        {
                            println(outputOption + " specified without a value")
                            System.exit(-1)
                        }
                    }
                    case _ if( consumeNext) => consumeNext = false // do nothing
                    case _ if( !consumeNext) => {
                        println("Unknown option: " + outputOption)
                        System.exit(-1)
                    }
                }


                i = i + 1
            }
        )

        implicit val outputWriter = if( output == ""){ System.out } else { new PrintStream(new FileOutputStream(output),true)}

        implicit val checker = checkArchitectures(sadFiles, codeLocations)

        implicit val delimiter = ";"



        if (printOverview) {

            outputWriter.println(
                "Ensemble;EnsembleElementCount;ConstraintTypem;ConstraintKind;Constraint(Srcs/Trgts);ConstraintViolationCount"
            )
            (checker.getEnsembles.toList.sortBy {
                _.name
            }).foreach((e: Ensemble) => outputWriter.println(ensembleToString(e)))
        }

        if (printViolations) {
            checker.violations.foreach((v: Violation) => outputWriter.println(violationToString(v)))
        }


        if (printDisjunct) {
            var pairs : List[(Ensemble, Ensemble)] = Nil
            for (
                first <- checker.getEnsembles.filter((e:Ensemble) => !e.name.startsWith("@"));
                second <- checker.getEnsembles.filter(
                        (e:Ensemble) =>
                            e != first &&
                                    !first.allDescendents.contains(e) &&
                                    !first.allAncestors.contains(e) &&
                                    !e.name.startsWith("@"))
            ) {
                if( !pairs.contains((first,second)) && ! pairs.contains((second,first)))
                {
                    pairs = pairs :+ (first,second)
                }
            }

            val double = pairs.flatMap{ case(fst,snd) =>
                {
                    println("checking pair (" + fst.name + ", " + snd.name + ")")
                    val elemA = checker.ensembleElements(fst).asList
                    val elemB = checker.ensembleElements(snd).asList
                    val doubles = elemA.filter( elemB.contains(_) )
                    for( elem <- doubles ) yield (fst,snd, elem)
                }
            }
            double.foreach{
                case(fst,snd,elem) => outputWriter.println(fst.name + delimiter + snd.name + delimiter + elementToString(elem))
            }

        }

    }


    def checkArchitectures(sadFiles: Array[String], codeLocations: Array[String]): ArchitectureChecker =
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
                    )
                )
            }
        )
        compiler.finishOutgoing()


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


    def readSadFile(stream: InputStream): Seq[UnissonDefinition] =
    {
        val in = new BufferedReader(new InputStreamReader(stream))
        var result: Seq[UnissonDefinition] = Nil
        while (in.ready) {

            val s = in.readLine();
            if (s.trim().length() > 0 && !s.trim().startsWith("%") && !s.trim().startsWith(":-")) {
                result = result :+ readPrologLine(s)
            }
        }

        ResolveAST(result)
    }

    def resourceAsStream(name: String) =
    {
        this.getClass.getClassLoader.getResourceAsStream(name)
    }

    def fileNameAsStream(name: String) =
    {
        val file = new File(name)
        new FileInputStream(file)
    }


    def readPrologLine(s: String): UnissonDefinition =
    {
        // TODO move functor recognition to the parser
        val functor = ISOPrologStringConversion.getFunctor(s)
        if (functor == ensembleFunctor) {
            return readEnsemble(s)
        }
        else if (dependencyConstraintFunctors.contains(functor)) {
            return readDependencyConstraint(s)
        }
        throw new IllegalArgumentException("can not parse the following string: " + s)
    }

    def readEnsemble(s: String): UnresolvedEnsemble =
    {
        val result = parser.parseAll(parser.ensemble, s)
        result match {
            case parser.Failure(msg, next) => {
                println("unable to parse ensemble:")
                println(msg)
                println(next.pos.longString)
                System.exit(-1)
                null
            }
            case parser.Success(ensemble, _) => ensemble
        }
    }

    def readDependencyConstraint(s: String): DependencyConstraint =
    {
        val result = parser.parseAll(parser.dependencyConstraint, s)
        result match {
            case parser.Failure(msg, next) => {
                println("unable to parse dependency:")
                println(msg)
                println(next.pos.longString)
                System.exit(-1)
                null
            }
            case parser.Success(dependency, _) => dependency
        }
    }


}