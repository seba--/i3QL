package unisson

import java.io._
import queries.QueryCompiler
import unisson.ast._
import Utilities._
import sae.collections.QueryResult
import sae.bytecode.model.dependencies.Dependency
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.ObjectType
import sae.LazyView
import sae.bytecode.MaterializedDatabase

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

    private val ensemble = "--ensemble"

    private val checkQueries = "--checkQueries"

    private val constraints = "--constraints"

    private val violations = "--violations"

    private val dependencies = "--dependencies"

    private val showRest = "--rest"

    private val verbose = "--verbose"

    private val aggregate = "--aggregated"

    private val createSad = "--createSad"

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
                |""" + ensemble + """ <Name>: outputs the elements of the ensemble with the given <Name>
                |""" + constraints + """ : outputs all constraints with counts for constraint violations
                |""" + dependencies + """ : outputs all dependencies for the modeled ensembles with a count for the dependencies.
                |""" + showRest + """ : outputs all elements that are not contained in an ensemble
                |""" + verbose + """ : outputs more data for other options, e.g. all elements in the dependency graph
                |""" + aggregate + """ : outputs aggregated counts
                |""" + createSad + """ <sadFile> creates a sad file, .e.g. to depict all dependencies
                |""" + checkQueries + """ sanity check all parts of the queries to see which sub queries do not select elements
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
        var printDependencies = false
        var isVerbose = false
        var output = ""
        var sadFileOut = ""
        var printEnsemble = ""
        var printQueryCheck = false
        var printAggregated = false

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
                    case _ if s == dependencies => printDependencies = true
                    case _ if s == verbose => isVerbose = true
                    case _ if s == aggregate => printAggregated = true
                    case _ if s == checkQueries => printQueryCheck = true
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

                    case _ if s == ensemble => {
                        if (i + 1 <= trail.size - 1) {
                            printEnsemble = trail(i + 1)
                            consumeNext = true
                        }
                        else {
                            println(ensemble + " specified without a value")
                            System.exit(-1)
                        }
                    }

                    case _ if s == createSad => {
                        if (i + 1 <= trail.size - 1) {
                            sadFileOut = trail(i + 1)
                            consumeNext = true
                        }
                        else {
                            println(createSad + " specified without a value")
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

        val sadWriter = if (sadFileOut == "") {
            System.out
        } else {
            new PrintStream(new FileOutputStream(sadFileOut), true)
        }

        implicit val checker = createChecker(sadFiles, printViolations)


        implicit val delimiter = ";"



        var storedDependencies : QueryResult[Dependency[AnyRef, AnyRef]] = null
        if (printDependencies) {

            storedDependencies = lazyViewToResult(checker.db.dependency)

        }

        var storedDatabase : MaterializedDatabase = null

        if( printQueryCheck )
        {
            storedDatabase = new MaterializedDatabase(checker.db)
        }

        readCode(checker, codeLocations)

        /*
            storedDependencies.foreach((d: Dependency[AnyRef, AnyRef]) =>
                                outputWriter.println(
                                            elementToString(new SourceElement(d.source)) + delimiter +
                                            elementToString(new SourceElement(d.target))
                                )
            )
        */
        if (printEnsemble != "") {
            if (checker.getEnsemble(printEnsemble) == None) {
                println("ensemble " + printEnsemble + " not found")
                System.exit(-1)
            }

            outputWriter.println("Type" + delimiter + "Element")

            checker.ensembleElements(checker.getEnsemble(printEnsemble).get).foreach(
                    (e: SourceElement[AnyRef]) => outputWriter.println(elementToString(e))
            )
        }

        if (printRest) {

            outputWriter.println("Type" + delimiter + "Element")

            checker.ensembleElements(checker.getEnsemble("@rest").get).foreach(
                    (e: SourceElement[AnyRef]) => outputWriter.println(elementToString(e))
            )
        }


        if (printEnsembles) {

            outputWriter.println("Ensemble" + delimiter + "Element Count" + delimiter + "Class Count" + delimiter + "Query")

            (checker.getEnsembles.toList.sortBy {
                _.name
            }).foreach(
                    (e: Ensemble) => {
                    var classes = 0
                    checker.ensembleElements(e).foreach {
                        case SourceElement(ObjectType(_)) => classes = classes + 1
                        case _ => // do nothing
                    }
                    outputWriter.println(ensembleToString(e) + delimiter + classes + delimiter + UnissonQuery.asString(e.query))
                }
            )
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

        val dependencyPairs: Set[(Ensemble, Ensemble)] = for {
            first <- checker.getEnsembles;
            if (first.name != "empty");
            if (!first.name.startsWith("@"));
            second <- checker.getEnsembles;
            if (second.name != "empty");
            if (!second.name.startsWith("@"));
            if (!first.allDescendents.contains(second));
            if (!first.allAncestors.contains(second))
        } yield {
            (first, second)
        }

        if (printDependencies && sadFileOut == "") {


            println("writing depdencies ...")
            if( !printAggregated )
             {
            outputWriter.println("Source Ensemble" + delimiter + "Source Element Count" + delimiter + "Target Ensemble" + delimiter + "Target Element Count" + delimiter + "Dependency Count")
             }
            var aggregated : Map[Ensemble, (Int, Int, Int, Int)] = Map()
            dependencyPairs.foreach {
                case (source, target) => {

                    val sources = checker.ensembleElements(source)
                    val targets = checker.ensembleElements(target)
                    val query = createDependencyQuery(sources, targets, storedDependencies)
                    if( !printAggregated )
                    {
                        outputWriter.println(
                            ensembleToString(source) + delimiter +
                                    ensembleToString(target) + delimiter +
                                    query.size
                        )
                    }
                    else
                    {
                        val oldSourceCounts = aggregated.getOrElse( source, (0, 0, 0, 0))
                        val oldTargetCounts = aggregated.getOrElse( target, (0, 0, 0, 0))
                        val inc = if( query.size == 0 ){ 0 } else { 1 }
                        val sourceCounts = (oldSourceCounts._1 + inc, oldSourceCounts._2 + query.size, oldSourceCounts._3, oldSourceCounts._4 )
                        val targetCounts = (oldTargetCounts._1, oldTargetCounts._2, oldTargetCounts._3 + inc, oldTargetCounts._4 + query.size)

                        aggregated += source -> sourceCounts
                        aggregated += target -> targetCounts
                    }
                    if (isVerbose) {
                        query.foreach(
                                (d: Dependency[AnyRef, AnyRef]) =>
                                outputWriter.println(
                                    ensembleToString(source) + delimiter +
                                            ensembleToString(target) + delimiter +
                                            query.size + delimiter +
                                            dependencyAsKind(d) + delimiter +
                                            elementToString(new SourceElement(d.source)) + delimiter +
                                            elementToString(new SourceElement(d.target))
                                )
                        )
                    }
                    sources.clearObservers()
                    targets.clearObservers()
                    storedDependencies.clearObservers()
                }
            }
             if( printAggregated )
             {
                                      outputWriter.println(
                            "Ensemble" + delimiter +
                                    "outgoing #ensembles" + delimiter +
                                    "outgoing #total" + delimiter +
                                    "incoming #ensembles"  + delimiter +
                                    "incoming #total"  + delimiter +
                                    "in+out coming #ensembles"
                        )
                 for( (ensemble, (outE, out, incE, inc)) <- aggregated)
                 {
                     outputWriter.println(
                            ensemble.name + delimiter +
                                    outE + delimiter +
                                    out + delimiter +
                                    incE + delimiter +
                                    inc  + delimiter +
                                    (outE + incE)
                        )
                 }
             }
        }
        if (printDependencies && sadFileOut != "") {
            println("creating depdency diagram ...")
            var id = -1

            def nextId: Int =
            {
                id += 1;
                id
            }

            val diagramId = nextId

            var ensembleIds: Map[Ensemble, Int] = Map()

            sadWriter.println(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns=\"http://vespucci.editor/2011-06-01\" xmlns:notation=\"http://www.eclipse.org/gmf/runtime/1.0.2/notation\">"
            )

            sadWriter.println("<ShapesDiagram xmi:id=\"" + diagramId + "\">")

            for {ensemble <- checker.getEnsembles;
                 if (ensemble.name != "empty");
                 if (!ensemble.name.startsWith("@"))
            } {
                val sourceId = ensembleIds.getOrElse(
                ensemble, {
                    val id = nextId;
                    ensembleIds += {
                        ensemble -> id
                    };
                    id
                }
                )

                sadWriter.println(
                    "<shapes xmi:type=\"Ensemble\" xmi:id=\"" + sourceId + "\" name=\"" + ensemble.name + "\" query=\"" + (UnissonQuery.asString(
                        ensemble.query
                    )(("", ""), true)) + "\">\n"
                )

                for {
                    (source, target) <- dependencyPairs;

                    if (source != target);
                    if (source == ensemble);
                    if (source.children.isEmpty); // only draw dependencies between leafs
                    if (target.children.isEmpty) // only draw dependencies between leafs
                } {
                    val sources = checker.ensembleElements(source)
                    val targets = checker.ensembleElements(target)
                    val query = createDependencyQuery(sources, targets, storedDependencies)
                    if (query.size > 0)
                    {
                        val targetId = ensembleIds.getOrElse(
                        target, {
                            val id = nextId;
                            ensembleIds += {
                                target -> id
                            };
                            id
                        }
                        )
                        val kinds = query.asList.map((d: Dependency[AnyRef, AnyRef]) => dependencyAsKind(d)).distinct
                        val name = kinds.reduceRight(_ + "," + _)
                        sadWriter.println(
                            "<targetConnections xmi:type=\"Expected\" xmi:id=\"" + nextId +
                                    "\" source=\"" + sourceId + "\" target=\"" + targetId +
                                    "\" name=\"" + name + "[" + query.size + "]" + "\"/>\n"
                        )
                    }
                    // TODO this is not 100% correct, as we clear also observers vor violations, but for now this will do.
                    sources.clearObservers()
                    targets.clearObservers()
                    storedDependencies.clearObservers()

                }
                sadWriter.println("</shapes>")
            }
            sadWriter.println("</ShapesDiagram>\n  <notation:Diagram xmi:id=\"" + nextId + "\" type=\"Vespucci\" element=\"" + diagramId + "\" name=\"" + sadFileOut + "\" measurementUnit=\"Pixel\">\n\n  </notation:Diagram>\n</xmi:XMI>")

        }

        if (printDuplicates) {

            val pairs = uniquePairs(checker)
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


        if( printQueryCheck )
        {

            println("checking queries for empty selections ...")
            for( view <- checker.db.baseViews)
            {
                view.clearObservers()
            }
            outputWriter.println("Ensemble" + delimiter + "Query" + delimiter + "Empty SubQuery")

            implicit val queries = new Queries(storedDatabase)

            for( ensemble <- checker.getEnsembles)
            {
                val concreteQueries = collectConcreteSubQueries(ensemble.query)
                for( query <- concreteQueries; if(query != AllQuery() && query != RestQuery() ) )
                {
                    val view = lazyViewToResult(QueryCompiler.compileUnissonQuery(query))
                    if( view.size == 0)
                    {
                        outputWriter.println(ensemble.name + delimiter + UnissonQuery.asString(ensemble.query) + delimiter + UnissonQuery.asString(query))
                    }
                }
            }

        }
    }


    def createDependencyQuery(firstView: LazyView[SourceElement[AnyRef]], secondView: LazyView[SourceElement[AnyRef]], dependencies: QueryResult[Dependency[AnyRef, AnyRef]]): QueryResult[Dependency[AnyRef, AnyRef]] =
    {
        val dependencyQuery = (
                (dependencies, (_: Dependency[AnyRef, AnyRef]).source) ⋉ ((_: SourceElement[AnyRef]).element, firstView)
                ) ∩ (
                (dependencies, (_: Dependency[AnyRef, AnyRef]).target) ⋉ ((_: SourceElement[AnyRef]).element, secondView)
                )

        lazyViewToResult(dependencyQuery)
    }

    // collect concrete subqueries, i.e., everything that is not concerned with logical combination (or/without)
    def collectConcreteSubQueries(query : UnissonQuery) : Seq[UnissonQuery] =
    {
        query match
        {
            case ClassSelectionQuery(_, _) => List(query)
            case ClassQuery(_) => List(query)
            case ClassWithMembersQuery(_) => List(query)
            case PackageQuery(_) => List(query)
            case OrQuery(left, right) => collectConcreteSubQueries(left) ++ collectConcreteSubQueries(right)
            case WithoutQuery(left, right) => collectConcreteSubQueries(left) ++ collectConcreteSubQueries(right)
            case TransitiveQuery(_) => List(query)
            case SuperTypeQuery(innerQuery) => List(query)
            case EmptyQuery() => List(query)
            case AllQuery() => List(query)
            case RestQuery() => List(query)
            case _ => throw new IllegalArgumentException("Unknown query type: " + query)
        }
    }
}