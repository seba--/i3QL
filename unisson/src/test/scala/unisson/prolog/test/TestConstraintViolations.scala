package unisson.prolog.test

import org.junit.Test
import org.junit.Assert._
import unisson.prolog.CheckArchitectureFromProlog._
import sae.bytecode.BytecodeDatabase
import sae.bytecode.model._
import unisson.queries.QueryCompiler
import unisson._
import unisson.ast._
import de.tud.cs.st.bat.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:33
 *
 */

class TestConstraintViolations
{

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
                        ClassWithMembersQuery(ClassQuery("unisson.test.simplegraph.v3.directed.incoming", "C")),
                        List()
                    )
                ),
                SourceElement(ObjectType("unisson/test/simplegraph/v3/directed/incoming/C")),
                IncomingConstraint(
                    List(
                        Ensemble(
                            "A",
                            ClassWithMembersQuery(ClassQuery("unisson.test.simplegraph.v3.directed.incoming", "A")),
                            List()
                        )
                    ),
                    Ensemble(
                        "C",
                        ClassWithMembersQuery(ClassQuery("unisson.test.simplegraph.v3.directed.incoming", "C")),
                        List()
                    ),
                    "all"
                ),
                "all"
            ),

            violation
        )

    }
}