package unisson.prolog.test

import org.junit.{Test, Ignore}
import org.junit.Assert._
import unisson.ast._
import unisson.model.kinds.AllKind

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:40
 *
 */

class TestASTGeneration
{

    import unisson.CheckArchitectureFromProlog._

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
        }
        assertTrue(A != None)
        assertEquals(
            ClassWithMembersQuery(ClassSelectionQuery("unisson.test.simplegraph.v1.selfref", "A")),
            A.get.query
        )
    }

    @Test
    def testSimpleGraphV2Cycle()
    {

        val definitions = readSadFile(
            resourceAsStream(
                "unisson/prolog/test/simplegraph/v2/cycle/v2.cycle.expected_correct.sad.pl"
            )
        )

        val A = definitions.collectFirst {
            case e@Ensemble("A", _, _, _) => e
        }
        assertTrue(A != None)
        // TODO refactor model and query
        assertEquals(ClassWithMembersQuery(ClassSelectionQuery("opal.test.simplegraph.v2.cycle", "A")), A.get.query)



        val B = definitions.collectFirst {
            case e@Ensemble("B", _, _, _) => e
        }
        assertTrue(B != None)
        // TODO refactor model and query
        assertEquals(ClassWithMembersQuery(ClassSelectionQuery("opal.test.simplegraph.v2.cycle", "B")), B.get.query)


    }


    @Test
    def testOutgoingResolving()
    {
        val definitions = readSadFile(resourceAsStream("unisson/prolog/test/outgoing/inner_outgoing.sad.pl"))

        val innerA = (definitions.collectFirst {
            case e@Ensemble("InnerA", _, _, _) => e
        }).get

        assertEquals(
            List(
                OutgoingConstraint(
                    Ensemble(
                        "InnerA",
                        EmptyQuery(),
                        List(),
                        List()
                    ),
                    List(
                        Ensemble(
                            "InnerAsNeighbor",
                            EmptyQuery(),
                            List(),
                            List()
                        ),
                        Ensemble(
                            "OutersNeighbor",
                            EmptyQuery(),
                            List(),
                            List()
                        )
                    ),
                    AllKind
                )
            )
            ,
            innerA.outgoingConnections
        )

        val outer = (definitions.collectFirst {
            case e@Ensemble("Outer", _, _, _) => e
        }).get


        assertEquals(
            List(
                OutgoingConstraint(
                    Ensemble(
                        "Outer",
                        EmptyQuery(),
                        List(),
                        List(
                            Ensemble("InnerA", EmptyQuery(), List(), List()),
                            Ensemble("InnerB", EmptyQuery(), List(), List())
                        )
                    ),
                    List(
                        Ensemble(
                            "OutersNeighbor",
                            EmptyQuery(),
                            List(),
                            List()
                        )
                    ),
                    AllKind
                )
            )
            ,
            outer.outgoingConnections
        )


    }


    @Test
    def testOutgoingSubsumption()
    {
        val definitions = readSadFile(resourceAsStream("unisson/prolog/test/outgoing/inner_outgoing.sad.pl"))

        val innerA = (definitions.collectFirst {
            case e@Ensemble("InnerA", _, _, _) => e
        }).get

        val innerB = (definitions.collectFirst {
            case e@Ensemble("InnerA", _, _, _) => e
        }).get

        val outer = (definitions.collectFirst {
            case e@Ensemble("Outer", _, _, _) => e
        }).get

        innerA.outgoingConnections.foreach(println)
        println("---------------------------------------------------")
        innerB.outgoingConnections.foreach(println)
        println("---------------------------------------------------")
        outer.outgoingConnections.foreach(println)

    }
}