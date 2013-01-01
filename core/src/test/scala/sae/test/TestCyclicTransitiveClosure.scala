package sae.test

import org.junit.Test
import org.junit.Assert._
import sae.{Relation, SetExtent}
import sae.operators.impl.CyclicTransitiveClosureView


/**
 * Test suit for HashTrainsitiveClosure
 * @author Malte V
 * @author Ralf Mitschke
 */
class TestCyclicTransitiveClosure
{

    def TC[T, V](relation: Relation[T], head: T => V, tail: T => V): Relation[(V, V)] =
        new CyclicTransitiveClosureView (relation, head, tail)

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
                ("c", "b"),
                ("c", "d"),
                ("c", "e"),
                ("d", "b"),
                ("d", "c"),
                ("d", "e")
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
                ("c", "b"),
                ("c", "d"),
                ("c", "e"),
                ("d", "BACK"),
                ("d", "b"),
                ("d", "c"),
                ("d", "e"),
                ("e", "BACK")
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
}