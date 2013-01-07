package sae.test

import org.junit.Test
import org.junit.Assert._
import sae.{Relation, SetExtent}
import sae.operators.impl.TransactionalCyclicTransitiveClosureView


/**
 * Test suit for HashTrainsitiveClosure
 * @author Malte V
 * @author Ralf Mitschke
 */
class TestTransactionalCyclicTransitiveClosure
{

    def TC[T, V](relation: Relation[T], head: T => V, tail: T => V): Relation[(V, V)] =
        new TransactionalCyclicTransitiveClosureView (relation, head, tail)

    case class Edge(start: String, end: String)

    def edgeHead: Edge => String = _.end

    def edgeTail: Edge => String = _.start

    @Test
    def testLinearChainAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "d"),
                ("c", "e"),
                ("d", "e")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testLinearChainRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.element_removed (Edge ("b", "c"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("c", "d"),
                ("c", "e"),
                ("d", "e")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testAcyclicMultiplePathsAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("c", "a"))
        edges.element_added (Edge ("c", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("c", "a"),
                ("c", "b")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testAcyclicMultiplePathsRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("c", "a"))
        edges.element_added (Edge ("c", "b"))

        edges.element_removed (Edge ("a", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("c", "a"),
                ("c", "b")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testAcyclicMultiplePathsChainAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("1", "2"))
        edges.element_added (Edge ("2", "3"))
        edges.element_added (Edge ("3", "a"))
        edges.element_added (Edge ("3", "c"))
        edges.element_added (Edge ("c", "a"))
        edges.element_added (Edge ("c", "b"))
        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "4"))
        edges.element_added (Edge ("4", "5"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("1", "2"),
                ("1", "3"),
                ("1", "4"),
                ("1", "5"),
                ("1", "a"),
                ("1", "b"),
                ("1", "c"),

                ("2", "3"),
                ("2", "4"),
                ("2", "5"),
                ("2", "a"),
                ("2", "b"),
                ("2", "c"),

                ("3", "4"),
                ("3", "5"),
                ("3", "a"),
                ("3", "b"),
                ("3", "c"),

                ("4", "5"),

                ("a", "4"),
                ("a", "5"),
                ("a", "b"),

                ("b", "4"),
                ("b", "5"),

                ("c", "4"),
                ("c", "5"),
                ("c", "a"),
                ("c", "b")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testAcyclicMultiplePathsChainRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("1", "2"))
        edges.element_added (Edge ("2", "3"))
        edges.element_added (Edge ("3", "a"))
        edges.element_added (Edge ("3", "c"))
        edges.element_added (Edge ("c", "a"))
        edges.element_added (Edge ("c", "b"))
        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "4"))
        edges.element_added (Edge ("4", "5"))

        edges.element_removed (Edge ("a", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("1", "2"),
                ("1", "3"),
                ("1", "4"),
                ("1", "5"),
                ("1", "a"),
                ("1", "b"),
                ("1", "c"),

                ("2", "3"),
                ("2", "4"),
                ("2", "5"),
                ("2", "a"),
                ("2", "b"),
                ("2", "c"),

                ("3", "4"),
                ("3", "5"),
                ("3", "a"),
                ("3", "b"),
                ("3", "c"),

                ("4", "5"),

                ("b", "4"),
                ("b", "5"),

                ("c", "4"),
                ("c", "5"),
                ("c", "a"),
                ("c", "b")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testBackEdgeCycleAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("c", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("b", "b"),
                ("b", "c"),
                ("b", "d"),
                ("c", "b"),
                ("c", "c"),
                ("c", "d")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testBackEdgeCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("c", "b"))

        edges.element_removed (Edge ("b", "c"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("c", "b"),
                ("c", "d")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriangleCycleAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.element_added (Edge ("d", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "b"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "b"),
                ("c", "c"),
                ("c", "d"),
                ("c", "e"),
                ("d", "b"),
                ("d", "c"),
                ("d", "d"),
                ("d", "e")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriangleCycleFrontAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.element_added (Edge ("d", "b"))

        edges.element_added (Edge ("_FRONT", "a"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("_FRONT", "a"),
                ("_FRONT", "b"),
                ("_FRONT", "c"),
                ("_FRONT", "d"),
                ("_FRONT", "e"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "b"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "b"),
                ("c", "c"),
                ("c", "d"),
                ("c", "e"),
                ("d", "b"),
                ("d", "c"),
                ("d", "d"),
                ("d", "e")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriangleCycleFrontAdditionAndCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.element_added (Edge ("d", "b"))

        edges.element_added (Edge ("_FRONT", "a"))

        edges.element_removed (Edge ("d", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("_FRONT", "a"),
                ("_FRONT", "b"),
                ("_FRONT", "c"),
                ("_FRONT", "d"),
                ("_FRONT", "e"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "d"),
                ("c", "e"),
                ("d", "e")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testQuadCycleFrontAdditionAndCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))
        edges.element_added (Edge ("e", "f"))

        edges.element_added (Edge ("e", "b"))

        edges.element_added (Edge ("_FRONT", "a"))

        edges.element_removed (Edge ("e", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("_FRONT", "a"),
                ("_FRONT", "b"),
                ("_FRONT", "c"),
                ("_FRONT", "d"),
                ("_FRONT", "e"),
                ("_FRONT", "f"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("a", "f"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("b", "f"),
                ("c", "d"),
                ("c", "e"),
                ("c", "f"),
                ("d", "e"),
                ("d", "f"),
                ("e", "f")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriangleCycleBackAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.element_added (Edge ("d", "b"))

        edges.element_added (Edge ("e", "BACK"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "BACK"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "BACK"),
                ("b", "b"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "BACK"),
                ("c", "b"),
                ("c", "c"),
                ("c", "d"),
                ("c", "e"),
                ("d", "BACK"),
                ("d", "b"),
                ("d", "c"),
                ("d", "d"),
                ("d", "e"),
                ("e", "BACK")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriangleCycleBackAdditionAndCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))

        edges.element_added (Edge ("d", "b"))

        edges.element_added (Edge ("e", "BACK"))

        edges.element_removed (Edge ("d", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "BACK"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "BACK"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "BACK"),
                ("c", "d"),
                ("c", "e"),
                ("d", "BACK"),
                ("d", "e"),
                ("e", "BACK")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testQuadCycleBackAdditionAndCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))
        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))
        edges.element_added (Edge ("e", "f"))

        edges.element_added (Edge ("e", "b"))

        edges.element_added (Edge ("f", "BACK"))

        edges.element_removed (Edge ("e", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "BACK"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("a", "f"),
                ("b", "BACK"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("b", "f"),
                ("c", "BACK"),
                ("c", "d"),
                ("c", "e"),
                ("c", "f"),
                ("d", "BACK"),
                ("d", "e"),
                ("d", "f"),
                ("e", "BACK"),
                ("e", "f"),
                ("f", "BACK")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriangleCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))

        edges.element_added (Edge ("d", "b"))

        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))


        edges.element_removed (Edge ("d", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("c", "d"),
                ("c", "e"),
                ("d", "e")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriStarOutgoingFromCycleAdditions() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "1"))
        edges.element_added (Edge ("b", "2"))
        edges.element_added (Edge ("c", "3"))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "a"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "1"),
                ("a", "2"),
                ("a", "3"),
                ("a", "a"),
                ("a", "b"),
                ("a", "c"),
                ("b", "1"),
                ("b", "2"),
                ("b", "3"),
                ("b", "a"),
                ("b", "b"),
                ("b", "c"),
                ("c", "1"),
                ("c", "2"),
                ("c", "3"),
                ("c", "a"),
                ("c", "b"),
                ("c", "c")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriStarOutgoingFromCycleAdditionsCycleFirst() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "a"))

        edges.element_added (Edge ("a", "1"))
        edges.element_added (Edge ("b", "2"))
        edges.element_added (Edge ("c", "3"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "1"),
                ("a", "2"),
                ("a", "3"),
                ("a", "a"),
                ("a", "b"),
                ("a", "c"),
                ("b", "1"),
                ("b", "2"),
                ("b", "3"),
                ("b", "a"),
                ("b", "b"),
                ("b", "c"),
                ("c", "1"),
                ("c", "2"),
                ("c", "3"),
                ("c", "a"),
                ("c", "b"),
                ("c", "c")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriStarOutgoingFromCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "a"))

        edges.element_added (Edge ("a", "1"))
        edges.element_added (Edge ("b", "2"))
        edges.element_added (Edge ("c", "3"))

        edges.element_removed (Edge ("c", "a"))

        edges.element_removed (Edge ("a", "b"))

        edges.element_removed (Edge ("b", "c"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "1"),
                ("b", "2"),
                ("c", "3")
            ),
            tc.asList.sorted
        )
    }


    @Test
    def testTriStarIncomingFromCycleAdditions() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("1", "a"))
        edges.element_added (Edge ("2", "b"))
        edges.element_added (Edge ("3", "c"))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "a"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("1", "a"),
                ("1", "b"),
                ("1", "c"),
                ("2", "a"),
                ("2", "b"),
                ("2", "c"),
                ("3", "a"),
                ("3", "b"),
                ("3", "c"),
                ("a", "a"),
                ("a", "b"),
                ("a", "c"),
                ("b", "a"),
                ("b", "b"),
                ("b", "c"),
                ("c", "a"),
                ("c", "b"),
                ("c", "c")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriStarIncomingFromCycleAdditionsCycleFirst() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))
        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "a"))

        edges.element_added (Edge ("1", "a"))
        edges.element_added (Edge ("2", "b"))
        edges.element_added (Edge ("3", "c"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("1", "a"),
                ("1", "b"),
                ("1", "c"),
                ("2", "a"),
                ("2", "b"),
                ("2", "c"),
                ("3", "a"),
                ("3", "b"),
                ("3", "c"),
                ("a", "a"),
                ("a", "b"),
                ("a", "c"),
                ("b", "a"),
                ("b", "b"),
                ("b", "c"),
                ("c", "a"),
                ("c", "b"),
                ("c", "c")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def testTriStarIncomingFromCycleRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "a"))

        edges.element_added (Edge ("1", "a"))
        edges.element_added (Edge ("2", "b"))
        edges.element_added (Edge ("3", "c"))

        edges.element_removed (Edge ("c", "a"))
        edges.element_removed (Edge ("a", "b"))
        edges.element_removed (Edge ("b", "c"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("1", "a"),
                ("2", "b"),
                ("3", "c")
            ),
            tc.asList.sorted
        )
    }


    @Test
    def tesOverlappingCycleAddition() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))
        edges.element_added (Edge ("e", "f"))



        edges.element_added (Edge ("d", "c"))
        edges.element_added (Edge ("e", "b"))
        edges.element_added (Edge ("f", "a"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "a"),
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("a", "f"),

                ("b", "a"),
                ("b", "b"),
                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("b", "f"),

                ("c", "a"),
                ("c", "b"),
                ("c", "c"),
                ("c", "d"),
                ("c", "e"),
                ("c", "f"),

                ("d", "a"),
                ("d", "b"),
                ("d", "c"),
                ("d", "d"),
                ("d", "e"),
                ("d", "f"),

                ("e", "a"),
                ("e", "b"),
                ("e", "c"),
                ("e", "d"),
                ("e", "e"),
                ("e", "f"),

                ("f", "a"),
                ("f", "b"),
                ("f", "c"),
                ("f", "d"),
                ("f", "e"),
                ("f", "f")
            ),
            tc.asList.sorted
        )
    }

    @Test
    def tesOverlappingCycleAdditionAndRemoval() {
        val edges = new SetExtent[Edge]()
        val tc = sae.relationToResult (TC (edges, edgeTail, edgeHead))

        edges.element_added (Edge ("a", "b"))
        edges.element_added (Edge ("b", "c"))
        edges.element_added (Edge ("c", "d"))
        edges.element_added (Edge ("d", "e"))
        edges.element_added (Edge ("e", "f"))



        edges.element_added (Edge ("d", "c"))
        edges.element_added (Edge ("e", "b"))
        edges.element_added (Edge ("f", "a"))

        edges.element_removed (Edge ("d", "c"))
        edges.element_removed (Edge ("f", "a"))
        edges.element_removed (Edge ("e", "b"))

        edges.notifyEndTransaction ()

        assertEquals (
            List (
                ("a", "b"),
                ("a", "c"),
                ("a", "d"),
                ("a", "e"),
                ("a", "f"),

                ("b", "c"),
                ("b", "d"),
                ("b", "e"),
                ("b", "f"),

                ("c", "d"),
                ("c", "e"),
                ("c", "f"),

                ("d", "e"),
                ("d", "f"),

                ("e", "f")

            ),
            tc.asList.sorted
        )

    }

}