package sae.test

import org.junit.Assert._
import org.junit.{Ignore, Before, Test}
import sae.collections.Table
import sae._
import util.Random
import operators.impl.{AcyclicTransitiveClosureView, NaiveTransitiveClosureView}


/**
 * Test suit for HashTrainsitiveClosure
 * @author Malte V
 * @author Ralf Mitschke
 */
@Ignore
class TestAcyclicTransitiveClosure extends org.scalatest.junit.JUnitSuite
{

    case class Vertex(id: String)

    case class Edge(start: Vertex, end: Vertex)

    object Edge
    {
        def apply(v1: String, v2: String) = {
            new Edge (Vertex (v1), Vertex (v2))
        }
    }

    var smallAddTestGraph: Table[Edge] = null
    var smallRemoveGraph : Table[Edge] = null
    var testGraph        : Table[Edge] = null

    @Test
    def addTest() {
        //test for figure 1 (a)
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](smallAddTestGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //smallAddTestGraph.materialized_foreach((x: Edge) => smallAddTestGraph.element_added(x))
        assertTrue (res.size == 2)
        smallAddTestGraph.element_added (Edge ("a", "b"))
        assertTrue (res.size == 6)

        assertTrue (res.asList.contains ((Vertex ("x"), Vertex ("a"))))
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        assertTrue (res.asList.contains ((Vertex ("x"), Vertex ("b"))))
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("y"))))
        assertTrue (res.asList.contains ((Vertex ("x"), Vertex ("y"))))
        assertTrue (res.asList.contains ((Vertex ("b"), Vertex ("y"))))
    }

    @Test
    def removeTest() {
        //test for figure 2
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](smallRemoveGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view

        assertTrue (res.size == 28)
        smallRemoveGraph.element_removed (Edge ("a", "b"))
        assertTrue (res.size == 25)
        assertTrue (res.asList.contains ((Vertex ("3"), Vertex ("b"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("a"))))
        assertTrue (!res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        smallRemoveGraph.element_removed (Edge ("c", "b"))
        assertTrue (res.size == 13)
        assertTrue (!res.asList.contains ((Vertex ("3"), Vertex ("b"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("a"))))
    }

    @Test
    def testSet() {
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("a", "b")
        // a -> b
        assertEquals (1 , res.size )
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        testGraph += Edge ("b", "e")
        // a -> b -> e
        assertEquals (3, res.size)
        assertTrue (res.asList.contains ((Vertex ("b"), Vertex ("e"))))
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("e"))))
        testGraph += Edge ("c", "d")
        // a -> b -> e
        // c -> d
        assertEquals (4, res.size)
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("d"))))
        testGraph += Edge ("d", "f")
        // a -> b -> e
        // c -> d -> f
        assertEquals (6, res.size)
        assertTrue (res.asList.contains ((Vertex ("d"), Vertex ("f"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("f"))))
        testGraph += Edge ("d", "e")
        // a -> b -> e
        //         ^
        //        /
        // c -> d -> f
        assertEquals (8, res.size)
        assertTrue (res.asList.contains ((Vertex ("d"), Vertex ("e"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("e"))))
        testGraph += Edge ("c", "a")
        // a -> b -> e
        // ^       ^
        // |      /
        // c -> d -> f
        assertEquals (10, res.size)
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("a"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("b"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("e"))))
        //remove some edges
        testGraph -= Edge ("c", "d")
        // a -> b -> e
        // ^       ^
        // |      /
        // c     d -> f
        assertEquals(8, res.size)
        assertTrue (!res.asList.contains ((Vertex ("c"), Vertex ("d"))))
        assertTrue (!res.asList.contains ((Vertex ("c"), Vertex ("f"))))
        assertTrue (res.asList.contains ((Vertex ("c"), Vertex ("e"))))
        testGraph -= Edge ("a", "b")
        // a    b -> e
        // ^       ^
        // |      /
        // c     d -> f
        assertEquals (4, res.size)
        assertTrue (!res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        assertTrue (!res.asList.contains ((Vertex ("a"), Vertex ("e"))))
        assertTrue (!res.asList.contains ((Vertex ("c"), Vertex ("e"))))
        assertTrue (!res.asList.contains ((Vertex ("c"), Vertex ("b"))))
    }

    @Test
    def testSetSemantic() {
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("a", "b")
        assertTrue (res.size == 1)
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        testGraph += Edge ("a", "b")
        assertTrue (res.size == 1)
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        testGraph -= Edge ("a", "b")
        assertTrue (res.size == 0)
    }


    @Test
    def testCycle() {
        //in general Cycles dont work!
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("a", "b")
        assertTrue (res.size == 1)
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        testGraph += Edge ("b", "c")
        assertTrue (res.size == 3)
        assertTrue (res.asList.contains ((Vertex ("b"), Vertex ("c"))))
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("c"))))
        testGraph += Edge ("c", "d")
        assertTrue (res.size == 6)
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("d"))))
        assertTrue (res.asList.contains ((Vertex ("b"), Vertex ("d"))))
        testGraph += Edge ("d", "e")
        assertTrue (res.size == 10)
        testGraph += Edge ("e", "a")
        assertTrue (res.size == 25)
        testGraph -= Edge ("b", "c")
        assertTrue (res.size == 10)
    }

    @Test
    def testSmall() {
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("a", "b")
        assertTrue (res.size == 1)
        testGraph += Edge ("b", "c")
        assertTrue (res.size == 3)
        testGraph += Edge ("a", "c")
        assertTrue (res.size == 3)
        assertTrue (res.asList.contains ((Vertex ("a"), Vertex ("b"))))
        testGraph -= Edge ("a", "b")
        assertTrue (res.size == 2)
    }

    @Test
    def testRemoveWithNoUpdateCase1() {
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("a", "b")
        testGraph += Edge ("a", "t1")
        testGraph += Edge ("t1", "t2")
        testGraph += Edge ("t2", "b")
        testGraph += Edge ("b", "y")
        assertTrue (res.size == 10)
        view.addObserver (getFailObs)
        testGraph -= Edge ("a", "b")
        assertTrue (res.size == 10)
    }

    @Test
    def testRemoveWithNoUpdateCase2() {
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("x", "a")
        testGraph += Edge ("a", "b")
        testGraph += Edge ("a", "t1")
        testGraph += Edge ("t1", "t2")
        testGraph += Edge ("t2", "b")
        testGraph += Edge ("b", "y")
        assertTrue (res.size == 15)
        view.addObserver (getFailObs)
        testGraph -= Edge ("a", "b")
        assertTrue (res.size == 15)
    }

    @Test
    def testRemoveWithNoUpdateCase3() {
        testGraph = new Table[Edge]
        val view: Relation[(Vertex, Vertex)] = new AcyclicTransitiveClosureView[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
        val res: QueryResult[(Vertex, Vertex)] = view
        //add some edges
        testGraph += Edge ("x", "a")
        testGraph += Edge ("a", "b")
        testGraph += Edge ("a", "t1")
        testGraph += Edge ("t1", "t2")
        testGraph += Edge ("t2", "b")
        assertTrue (res.size == 10)
        view.addObserver (getFailObs)
        testGraph -= Edge ("a", "b")
        assertTrue (res.size == 10)
    }

    @Before
    def initialTestSetting() {
        smallAddTestGraph = new Table[Edge]
        smallAddTestGraph += Edge ("x", "a")
        smallAddTestGraph += Edge ("b", "y")
        smallRemoveGraph = new Table[Edge]
        smallRemoveGraph += Edge ("1", "2")
        smallRemoveGraph += Edge ("2", "3")
        smallRemoveGraph += Edge ("3", "a")
        smallRemoveGraph += Edge ("3", "c")
        smallRemoveGraph += Edge ("a", "b")
        smallRemoveGraph += Edge ("c", "a")
        smallRemoveGraph += Edge ("c", "b")
        smallRemoveGraph += Edge ("b", "4")
        smallRemoveGraph += Edge ("4", "5")
    }

    @Test
    def bigRandomTest() {
        for (i <- 0 to 3)
        {
            rndTestHelpber ()
        }
    }

    def rndTestHelpber() {
        val size = 20
        val source: Relation[(java.lang.Integer, java.lang.Integer)] = new BagExtent[(java.lang.Integer, java.lang.Integer)]
        val tc = new AcyclicTransitiveClosureView (source, (_: (java.lang.Integer, java.lang.Integer))._1, (_: (java.lang.Integer, java.lang.Integer))._2)
        val queryResult: QueryResult[(java.lang.Integer, java.lang.Integer)] = tc
        val matrix = new Array[Array[java.lang.Integer]](size)
        var oldRes: Array[Array[java.lang.Integer]] = null
        for (i <- 0 to size - 1) {
            matrix (i) = new Array[java.lang.Integer](size)
        }
        for (i <- 0 to size * size * size) {
            var a = Random.nextInt (size)
            var b = Random.nextInt (size)
            var i = 0
            while (
                a == b
                    || (oldRes != null && oldRes (b)(a) == 1)
            )
            {
                a = Random.nextInt (size)
                b = Random.nextInt (size)
                i += 1
                if (i > 100000) throw new Error ("infinite loop break -- restart Test if it happens offten check test settings")
            }

            if (matrix (a)(b) == 1) {
                //remove
                //println("remove a: " + a + " b: " + b)
                matrix (a)(b) = 0
                oldRes = calcTC (matrix, size)
                source.element_removed ((a, b))
                val list = queryResult.asList
                for (u <- 0 to size - 1)
                    for (w <- 0 to size - 1) {
                        if (oldRes (u)(w) == 1) {
                            assertTrue (list.contains ((u, w)))
                        }
                        else
                        {
                            assertTrue (!list.contains ((u, w)))
                        }
                    }
            }
            else
            {
                //add
                //println("add a: " + a + " b: " + b)
                matrix (a)(b) = 1
                oldRes = calcTC (matrix, size)
                source.element_added ((a, b))
                val list = queryResult.asList
                for (u <- 0 to size - 1)
                    for (w <- 0 to size - 1) {
                        if (oldRes (u)(w) == 1) {
                            assertTrue (list.contains ((u, w)))
                        }
                        else
                        {
                            /*              if (list.contains((u, w))) {
                              println(u, w)
                              println("--")
                              list.foreach(println)
                              fail()
                            }*/
                            assertTrue (!list.contains (u, w))
                        }
                    }
            }
        }

    }

    /**
     * a non incremental calculation of transitive closure
     */
    private def calcTC(matrix: Array[Array[java.lang.Integer]], size: Int): Array[Array[java.lang.Integer]] = {

        val res = new Array[Array[java.lang.Integer]](size)
        var i = 0
        var j = 0
        var k = 0
        for (i <- 0 to size - 1) {
            res (i) = new Array[java.lang.Integer](size)
        }
        for (j <- 0 to size - 1)
            for (i <- 0 to size - 1)
                res (i)(j) = matrix (i)(j)

        for (j <- 0 to size - 1) {
            for (i <- 0 to size - 1) {
                if (res (i)(j) == 1) {
                    for (k <- 0 to size - 1) {
                        if (res (j)(k) == 1)
                            res (i)(k) = 1
                    }
                }
            }
        }

        res
    }


    private def getFailObs = {
        new Observer[AnyRef]
        {
            def updated(oldV: AnyRef, newV: AnyRef) {
                fail ()
            }

            def added(v: AnyRef) {
                fail ()
            }

            def removed(v: AnyRef) {
                fail ()
            }
        }
    }
}