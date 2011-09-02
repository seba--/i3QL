package unisson.prolog.test

import org.junit.Test
import unisson.prolog.CheckArchitectureFromProlog._
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import unisson.queries.QueryCompiler
import unisson.{SourceElement, ArchitectureChecker}
import sae.bytecode.model._
import de.tud.cs.st.bat._
import unisson.ast.{Ensemble, UnresolvedEnsemble}

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:33
 *
 */

class TestEnsembleQueries
{

    @Test
    def testSimpleGraphV1()
    {
        val definitions = readSadFile(
            resourceAsStream(
                "unisson/prolog/test/simplegraph/v1/selfref/v1.selfref.ensemble.sad.pl"
            )
        )
        val A = definitions.collectFirst {
            case e@Ensemble("A", _, _,_) => e
        }.get


        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(definitions)

        db.transformerForClass(classOf[unisson.test.simplegraph.v1.selfref.A]).processAllFacts()

        val elements = checker.ensembleElements(A)

        assertTrue(
            elements.contains(
                SourceElement(
                    Method(
                        ObjectType("unisson/test/simplegraph/v1/selfref/A"),
                        "<init>",
                        List(),
                        VoidType()
                    )
                )
            )
        )
        assertTrue(
            elements.contains(
                SourceElement(
                    Field(
                        ObjectType("unisson/test/simplegraph/v1/selfref/A"),
                        "fieldRef",
                        ObjectType("unisson/test/simplegraph/v1/selfref/A")
                    )
                )
            )
        )

        assertTrue(elements.contains(SourceElement(ObjectType("unisson/test/simplegraph/v1/selfref/A"))))
    }

}