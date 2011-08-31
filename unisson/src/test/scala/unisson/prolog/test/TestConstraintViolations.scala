package unisson.prolog.test

import org.junit.Test
import unisson.prolog.CheckArchitectureFromProlog._
import sae.bytecode.BytecodeDatabase
import unisson.queries.QueryCompiler
import unisson.ArchitectureChecker
import unisson.ast.Ensemble

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

        checker.getEnsembles.foreach( (e:Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)

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


        checker.getEnsembles.foreach( (e:Ensemble) => println(checker.ensembleStatistic(e)))
        checker.violations.foreach(println)

    }
}