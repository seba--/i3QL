package unisson.prolog.test

import org.junit.Test
import unisson.CheckArchitectureFromProlog._
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import unisson.queries.QueryCompiler
import unisson.{SourceElement, ArchitectureChecker}
import sae.bytecode.model._
import de.tud.cs.st.bat._
import unisson.ast.Ensemble
import scala.Predef._

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
            case e@Ensemble("A", _, _, _) => e
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


    @Test
    def testTransitiveInheritance()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream("unisson/prolog/test/transitive/inheritance/allSubtypesIA.sad.pl")
            )
        )
        compiler.finishOutgoing()

        val allSubtypesIA = checker.ensembleElements(checker.getEnsemble("AllSubtypesIA").get)

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.IA] ).processAllFacts()

        assertEquals(0, allSubtypesIA.size)

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.IB] ).processAllFacts()


        assertEquals(1, allSubtypesIA.size)
        assertEquals(
            Some(SourceElement(
                ObjectType("unisson/test/transitive/inheritance/IB")
            )),
            allSubtypesIA.singletonValue
        )

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.C1] ).processAllFacts()

        assertEquals(2, allSubtypesIA.size)
        assertEquals(
            List(
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C1")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/IB")
                )
            ),
            allSubtypesIA.asList.sortBy( _.asInstanceOf[SourceElement[ObjectType]].element.className )
        )

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.C2] ).processAllFacts()

        assertEquals(3, allSubtypesIA.size)
        assertEquals(
            List(
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C1")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C2")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/IB")
                )
            ),
            allSubtypesIA.asList.sortBy( _.asInstanceOf[SourceElement[ObjectType]].element.className )
        )

    }

/*
        def testTransitiveInheritance()
    {
        val db = new BytecodeDatabase()

        val checker = new ArchitectureChecker(db)

        val compiler = new QueryCompiler(checker)

        compiler.addAll(
            readSadFile(
                resourceAsStream("unisson/prolog/test/transitive/inheritance/inheritance.sad.pl")
            )
        )
        compiler.finishOutgoing()


        val allSubTypesC2 = checker.ensembleElements(checker.getEnsemble("AllSubTypesC2").get)

        val allSubtypesIB = checker.ensembleElements(checker.getEnsemble("AllSubtypesIB").get)

        val allSubtypesIA = checker.ensembleElements(checker.getEnsemble("AllSubtypesIA").get)

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.IA] ).processAllFacts()

        assertEquals(0, allSubTypesC2.size)
        assertEquals(0, allSubtypesIB.size)
        assertEquals(0, allSubtypesIA.size)

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.IB] ).processAllFacts()

        assertEquals(0, allSubTypesC2.size)
        assertEquals(0, allSubtypesIB.size)
        assertEquals(1, allSubtypesIA.size)
        assertEquals(
            Some(SourceElement(
                ObjectType("unisson/test/transitive/inheritance/IB")
            )),
            allSubtypesIA.singletonValue
        )

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.C1] ).processAllFacts()

        assertEquals(0, allSubTypesC2.size)
        assertEquals(0, allSubtypesIB.size)
        assertEquals(2, allSubtypesIA.size)
        assertEquals(
            List(
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C1")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/IB")
                )
            ),
            allSubtypesIA.asList.sortBy( _.asInstanceOf[SourceElement[ObjectType]].element.className )
        )

        db.transformerForClass( classOf[unisson.test.transitive.inheritance.C2] ).processAllFacts()

        assertEquals(0, allSubTypesC2.size)
        assertEquals(1, allSubtypesIB.size)
        assertEquals(
            List(
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C2")
                )
            ),
            allSubtypesIB.asList.sortBy( _.asInstanceOf[SourceElement[ObjectType]].element.className )
        )
        assertEquals(3, allSubtypesIA.size)
        assertEquals(
            List(
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C1")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C2")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/IB")
                )
            ),
            allSubtypesIA.asList.sortBy( _.asInstanceOf[SourceElement[ObjectType]].element.className )
        )


        //allSubTypesC2.foreach(println)
        /*
        assertEquals(1, allSubTypesC2.size)
        assertEquals(
            SourceElement(
                ObjectType("unisson/test/transitive/inheritance/C3")
            ),
            allSubTypesC2.singletonValue
        )
        */

        //println("-----------------------------------------------------")
        //allSubtypesIB.foreach(println)
        /*
        assertEquals(2, allSubtypesIB.size)
        assertEquals(
            List(
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C2")
                ),
                SourceElement(
                    ObjectType("unisson/test/transitive/inheritance/C3")
                )
            ),
            allSubtypesIB.asList
        )
        */
    }
*/
}