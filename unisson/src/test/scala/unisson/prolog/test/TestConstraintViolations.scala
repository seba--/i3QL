package unisson.prolog.test

import org.junit.{Test, Ignore}
import org.junit.Assert._
import unisson.CheckArchitectureFromProlog._
import sae.bytecode.BytecodeDatabase
import sae.bytecode.model._
import dependencies.Dependency
import unisson.queries.QueryCompiler
import unisson._
import unisson.ast._
import de.tud.cs.st.bat.ObjectType
import sae.syntax.RelationalAlgebraSyntax._
import sae.operators.Aggregation
import sae.functions.Count
import sae.collections.QueryResult

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:33
 *
 */

class TestConstraintViolations
{

    @Test
    def testSimpleGraphExpectedNoViolation()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v2/directed/v2.directed.expected_correct.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v2.directed.A],
                classOf[unisson.test.simplegraph.v2.directed.B]
            )
        ).processAllFacts()

        /*
        checker.getEnsembles.foreach((e: Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)
        */

        assertEquals(0, checker.violations.size)
    }

    @Ignore
    @Test
    def testSimpleGraphExpectedViolation()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v2/directed/v2.directed.expected_violation.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()


        val A = checker.getEnsembles.collectFirst {
            case e@Ensemble("A", _, _, _) => e
        }.get
        val B = checker.getEnsembles.collectFirst {
            case e@Ensemble("B", _, _, _) => e
        }.get

        val sourceQuery = checker.ensembleElements(B)

        val targetQuery = checker.ensembleElements(A)

        val dependencyRelation = db.dependency

        val sourceToTargetDep = ((dependencyRelation, Queries.source(_)) ⋉ (identity(_: SourceElement[AnyRef]), sourceQuery)) ∩
                ((dependencyRelation, Queries.target(_)) ⋉ (identity(_: SourceElement[AnyRef]), targetQuery))

        val res1: QueryResult[Dependency[AnyRef, AnyRef]] = sourceToTargetDep

        val aggregation = Aggregation[Dependency[AnyRef, AnyRef], None.type, Int](
            sourceToTargetDep,
                (newD: Dependency[AnyRef, AnyRef]) => None,
            Count[Dependency[AnyRef, AnyRef]]()
        )



        val countQuery = σ((_: (None.type, Int))._2 == 0)(aggregation)



        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v2.directed.A],
                classOf[unisson.test.simplegraph.v2.directed.B]
            )
        ).processAllFacts()

        res1.foreach(println)

        checker.getEnsembles.foreach((e: Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)

        assertEquals(1, checker.violations.size)

        assertEquals(
            Violation(
                Some(
                    Ensemble(
                        "B",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "B")),
                        List(),
                        List()
                    )
                ),
                null,
                Some(
                    Ensemble(
                        "A",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "A")),
                        List(),
                        List()
                    )
                ),
                null,
                ExpectedConstraint(
                    Ensemble(
                        "B",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "B")),
                        List(),
                        List()
                    ),
                    Ensemble(
                        "A",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "A")),
                        List(),
                        List()
                    ),
                    "all"
                ),
                "none"
            ),
            checker.violations.singletonValue.get
        )

    }


    @Test
    def testSimpleGraphNotAllowedNoViolation()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v2/directed/v2.directed.not_allowed_correct.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v2.directed.A],
                classOf[unisson.test.simplegraph.v2.directed.B]
            )
        ).processAllFacts()

        /*
        checker.getEnsembles.foreach((e: Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)
        */

        assertEquals(0, checker.violations.size)
    }

    @Test
    def testSimpleGraphNotAllowedViolation()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v2/directed/v2.directed.not_allowed_violation.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v2.directed.A],
                classOf[unisson.test.simplegraph.v2.directed.B]
            )
        ).processAllFacts()

        /*
        checker.getEnsembles.foreach((e: Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)
        */
        assertEquals(1, checker.violations.size)

        assertEquals(
            Violation(
                Some(
                    Ensemble(
                        "A",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "A")),
                        List(),
                        List()
                    )
                ),
                SourceElement(
                    Field(
                        ObjectType("unisson/test/simplegraph/v2/directed/A"),
                        "fieldRef",
                        ObjectType("unisson/test/simplegraph/v2/directed/B")
                    )
                ),
                Some(
                    Ensemble(
                        "B",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "B")),
                        List(),
                        List()
                    )
                ),
                SourceElement(ObjectType(className = "unisson/test/simplegraph/v2/directed/B")),
                NotAllowedConstraint(
                    Ensemble(
                        "A",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "A")),
                        List(),
                        List()
                    ),
                    Ensemble(
                        "B",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v2.directed", "B")),
                        List(),
                        List()
                    )
                    , "all"
                ),
                "field_type"
            ),
            checker.violations.singletonValue.get
        )

    }

    @Test
    def testSimpleGraphIncomingNoViolation()
    {
        val definitions = readSadFile(
            resourceAsStream(
                "unisson/prolog/test/simplegraph/v3/directed/incoming/v3.directed.incoming.correct.sad.pl"
            )
        )
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(definitions)
        compiler.finishOutgoing()

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v3.directed.incoming.A],
                classOf[unisson.test.simplegraph.v3.directed.incoming.B],
                classOf[unisson.test.simplegraph.v3.directed.incoming.C]
            )
        ).processAllFacts()

        /*
        checker.getEnsembles.foreach((e: Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)
        */
        assertEquals(0, checker.violations.size)
    }

    @Test
    def testSimpleGraphIncomingViolation()
    {

        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v3/directed/incoming/v3.directed.incoming.violation.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v3.directed.incoming.A],
                classOf[unisson.test.simplegraph.v3.directed.incoming.B],
                classOf[unisson.test.simplegraph.v3.directed.incoming.C]
            )
        ).processAllFacts()

        /*
        checker.getEnsembles.foreach( (e:Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)
        */

        assertEquals(1, checker.violations.size)

        val violation = checker.violations.singletonValue.get
        assertEquals(
            Violation(
                None,
                SourceElement(
                    Field(
                        ObjectType("unisson/test/simplegraph/v3/directed/incoming/B"),
                        "fieldRef",
                        ObjectType("unisson/test/simplegraph/v3/directed/incoming/C")
                    )
                ),
                Some(
                    Ensemble(
                        "C",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v3.directed.incoming", "C")),
                        List(),
                        List()
                    )
                ),
                SourceElement(ObjectType("unisson/test/simplegraph/v3/directed/incoming/C")),
                IncomingConstraint(
                    List(
                        Ensemble(
                            "A",
                            ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v3.directed.incoming", "A")),
                            List(),
                            List()
                        )
                    ),
                    Ensemble(
                        "C",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v3.directed.incoming", "C")),
                        List(),
                        List()
                    ),
                    "all"
                ),
                "field_type"
            ),

            violation
        )

    }


    @Test
    def testSimpleGraphOutgoingNoViolation()
    {

        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v3/directed/outgoing/v3.directed.outgoing.correct.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v3.directed.outgoing.A],
                classOf[unisson.test.simplegraph.v3.directed.outgoing.B],
                classOf[unisson.test.simplegraph.v3.directed.outgoing.C],
                classOf[unisson.test.simplegraph.v3.directed.outgoing.D]
            )
        ).processAllFacts()


        checker.violations.foreach(println)
        assertEquals(0, checker.violations.size)

    }

    @Test
    def testSimpleGraphOutgoingViolation()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream(
                    "unisson/prolog/test/simplegraph/v3/directed/outgoing/v3.directed.outgoing.violation.sad.pl"
                )
            )
        )
        compiler.finishOutgoing()

        /*
        val A = checker.getEnsembles.collectFirst{ case e @ Ensemble("A",_,_) => e }.get
        val B = checker.getEnsembles.collectFirst{ case e @ Ensemble("B",_,_) => e }.get
        val C = checker.getEnsembles.collectFirst{ case e @ Ensemble("C",_,_) => e }.get

        val dependencyRelation = checker.db.dependency
        val query = Conversions.lazyViewToResult(
            ((dependencyRelation, Queries.source(_)) ⋉ (identity(_:SourceElement[AnyRef]), checker.ensembleElements(A)) ) ∩
        ( (dependencyRelation, Queries.target(_)) ⊳ (identity(_:SourceElement[AnyRef]), checker.ensembleElements(A)) )∩
        ((dependencyRelation, Queries.target(_)) ⊳ (identity(_:SourceElement[AnyRef]), checker.ensembleElements(B))) ∩
        ((dependencyRelation, Queries.target(_)) ⋉ (identity(_:SourceElement[AnyRef]), checker.ensembleElements(C))))
        */

        db.transformerForClasses(
            Array(
                classOf[unisson.test.simplegraph.v3.directed.outgoing.A],
                classOf[unisson.test.simplegraph.v3.directed.outgoing.B],
                classOf[unisson.test.simplegraph.v3.directed.outgoing.C],
                classOf[unisson.test.simplegraph.v3.directed.outgoing.D]
            )
        ).processAllFacts()


        //checker.getEnsembles.foreach((e: Ensemble) => println(checker.ensembleStatistic(e)))


        assertEquals(
            Violation(
                Some(
                    Ensemble(
                        "A",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v3.directed.outgoing", "A")),
                        List(),
                        List()
                    )
                ),
                SourceElement(
                    Field(
                        ObjectType("unisson/test/simplegraph/v3/directed/outgoing/A"),
                        "fieldRefC",
                        ObjectType("unisson/test/simplegraph/v3/directed/outgoing/C")
                    )
                ),
                None,
                SourceElement(ObjectType("unisson/test/simplegraph/v3/directed/outgoing/C")),
                OutgoingConstraint(
                    Ensemble(
                        "A",
                        ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v3.directed.outgoing", "A")),
                        List(),
                        List()
                    ),
                    List(
                        Ensemble(
                            "B",
                            ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v3.directed.outgoing", "B")),
                            List(),
                            List()
                        )
                    ),
                    "all"
                ),
                "field_type"
            ),
            checker.violations.singletonValue.get
        )


    }
}