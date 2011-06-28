package sae.test

import org.junit.Assert._
import org.junit.{Before, Test, Ignore}
import sae.collections.{QueryResult, Table}
import sae.operators.HashTransitiveClosure
import sae.syntax.RelationalAlgebraSyntax._
import sae.{Observer, LazyView}

/**
 * Date: 10.06.11
 * Time: 15:56
 * @author Malte V
 */

class TestTransitiveClosure extends org.scalatest.junit.JUnitSuite {

  case class Vertex(val id: String)

  case class Edge(val start: Vertex, val end: Vertex)

  object Edge {
    def apply(v1: String, v2: String) = {
      new Edge(Vertex(v1), Vertex(v2))
    }
  }

  var smallAddTestGraph: Table[Edge] = null
  var smallRemoveGraph: Table[Edge] = null
  var testGraph: Table[Edge] = null

  @Test
  def addTest {
    //test for figure 1 (a)
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](smallAddTestGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //smallAddTestGraph.materialized_foreach((x: Edge) => smallAddTestGraph.element_added(x))
    assertTrue(res.size == 2)
    smallAddTestGraph.element_added(Edge("a", "b"))
    assertTrue(res.size == 6)

    assertTrue(res.contains((Vertex("x"), Vertex("a"))))
    assertTrue(res.contains((Vertex("a"), Vertex("b"))))
    assertTrue(res.contains((Vertex("x"), Vertex("b"))))
    assertTrue(res.contains((Vertex("a"), Vertex("y"))))
    assertTrue(res.contains((Vertex("x"), Vertex("y"))))
    assertTrue(res.contains((Vertex("b"), Vertex("y"))))
  }

  @Test
  def removeTest {
    //test for figure 2
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](smallRemoveGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //smallRemoveGraph.materialized_foreach((x: Edge) => smallRemoveGraph.element_added(x))
    assertTrue(res.size == 28)
    smallRemoveGraph.element_removed(Edge("a", "b"))
    assertTrue(res.size == 25)
    assertTrue(res.asList.contains((Vertex("3"), Vertex("b"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("a"))))
    assertTrue(!res.asList.contains((Vertex("a"), Vertex("b"))))
    smallRemoveGraph.element_removed(Edge("c", "b"))
    assertTrue(res.size == 13)
    assertTrue(!res.asList.contains((Vertex("3"), Vertex("b"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("a"))))
  }

  @Test
  def testSet {
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("a", "b")
    assertTrue(res.size == 1)
    assertTrue(res.asList.contains((Vertex("a"), Vertex("b"))))
    testGraph += Edge("b", "e")
    assertTrue(res.size == 3)
    assertTrue(res.asList.contains((Vertex("b"), Vertex("e"))))
    assertTrue(res.asList.contains((Vertex("a"), Vertex("e"))))
    testGraph += Edge("c", "d")
    assertTrue(res.size == 4)
    assertTrue(res.asList.contains((Vertex("c"), Vertex("d"))))
    testGraph += Edge("d", "f")
    assertTrue(res.size == 6)
    assertTrue(res.asList.contains((Vertex("d"), Vertex("f"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("f"))))
    testGraph += Edge("d", "e")
    assertTrue(res.size == 8)
    assertTrue(res.asList.contains((Vertex("d"), Vertex("e"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("e"))))
    testGraph += Edge("c", "a")
    assertTrue(res.size == 10)
    assertTrue(res.asList.contains((Vertex("c"), Vertex("a"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("b"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("e"))))
    //remove some edges
    testGraph -= Edge("c", "d")
    assertTrue(res.size == 8)
    assertTrue(!res.asList.contains((Vertex("c"), Vertex("d"))))
    assertTrue(!res.asList.contains((Vertex("c"), Vertex("f"))))
    assertTrue(res.asList.contains((Vertex("c"), Vertex("e"))))
    testGraph -= Edge("a", "b")
    assertTrue(res.size == 4)
    assertTrue(!res.asList.contains((Vertex("a"), Vertex("b"))))
    assertTrue(!res.asList.contains((Vertex("a"), Vertex("e"))))
    assertTrue(!res.asList.contains((Vertex("c"), Vertex("e"))))
    assertTrue(!res.asList.contains((Vertex("c"), Vertex("b"))))
  }

  @Test
  def testSetSemantic {
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("a", "b")
    assertTrue(res.size == 1)
    assertTrue(res.asList.contains((Vertex("a"), Vertex("b"))))
    testGraph += Edge("a", "b")
    assertTrue(res.size == 1)
    assertTrue(res.asList.contains((Vertex("a"), Vertex("b"))))
    testGraph -= Edge("a", "b")
    assertTrue(res.size == 0)
  }


  @Test
  def testCycle {
    //Cycles dont work!
    //TODO: find a case where cycles dont work
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("a", "b")
    assertTrue(res.size == 1)
    assertTrue(res.asList.contains((Vertex("a"), Vertex("b"))))
    testGraph += Edge("b", "c")
    assertTrue(res.size == 3)
    assertTrue(res.asList.contains((Vertex("b"), Vertex("c"))))
    assertTrue(res.asList.contains((Vertex("a"), Vertex("c"))))
    testGraph += Edge("c", "d")
    assertTrue(res.size == 6)
    assertTrue(res.asList.contains((Vertex("a"), Vertex("d"))))
    assertTrue(res.asList.contains((Vertex("b"), Vertex("d"))))
    testGraph += Edge("d", "e")
    assertTrue(res.size == 10)
    testGraph += Edge("e", "a")
    assertTrue(res.size == 25)
    testGraph -= Edge("b", "c")
    assertTrue(res.size == 10)
  }

  @Test
  def testSmall {
    //Cycles dont work!
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("a", "b")
    assertTrue(res.size == 1)
    testGraph += Edge("b", "c")
    assertTrue(res.size == 3)
    testGraph += Edge("a", "c")
    assertTrue(res.size == 3)
    assertTrue(res.asList.contains((Vertex("a"), Vertex("b"))))
    testGraph -= Edge("a", "b")
    assertTrue(res.size == 2)
  }
  @Test
  def testRemoveWithNoUpdateCase1 {
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("a", "b")
    testGraph += Edge("a", "t1")
    testGraph += Edge("t1", "t2")
    testGraph += Edge("t2", "b")
    testGraph += Edge("b", "y")
    assertTrue(res.size == 10 )
    view.addObserver(getFailObs())
    testGraph -= Edge("a", "b")
    assertTrue(res.size == 10)
  }
  @Test
  def testRemoveWithNoUpdateCase2 {
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("x","a")
    testGraph += Edge("a", "b")
    testGraph += Edge("a", "t1")
    testGraph += Edge("t1", "t2")
    testGraph += Edge("t2", "b")
    testGraph += Edge("b", "y")
    assertTrue(res.size == 15 )
    view.addObserver(getFailObs())
    testGraph -= Edge("a", "b")
    assertTrue(res.size == 15)
  }
  @Test
  def testRemoveWithNoUpdateCase3 {
    testGraph = new Table[Edge]
    val view: LazyView[(Vertex, Vertex)] = new HashTransitiveClosure[Edge, Vertex](testGraph, (x: Edge) => x.start, (x: Edge) => x.end)
    val res: QueryResult[(Vertex, Vertex)] = view
    //add some edges
    testGraph += Edge("x","a")
    testGraph += Edge("a", "b")
    testGraph += Edge("a", "t1")
    testGraph += Edge("t1", "t2")
    testGraph += Edge("t2", "b")
    assertTrue(res.size == 10 )
    view.addObserver(getFailObs())
    testGraph -= Edge("a", "b")
    assertTrue(res.size == 10)
  }

  @Before
  def initialTestSetting {
    smallAddTestGraph = new Table[Edge]
    smallAddTestGraph += Edge("x", "a")
    smallAddTestGraph += Edge("b", "y")
    smallRemoveGraph = new Table[Edge]
    smallRemoveGraph += Edge("1", "2")
    smallRemoveGraph += Edge("2", "3")
    smallRemoveGraph += Edge("3", "a")
    smallRemoveGraph += Edge("3", "c")
    smallRemoveGraph += Edge("a", "b")
    smallRemoveGraph += Edge("c", "a")
    smallRemoveGraph += Edge("c", "b")
    smallRemoveGraph += Edge("b", "4")
    smallRemoveGraph += Edge("4", "5")
  }
  private def getFailObs() = {
    new Observer[AnyRef] {
      def updated(oldV: AnyRef, newV: AnyRef) {fail()}

      def added(v: AnyRef) {fail()}

      def removed(v: AnyRef) {fail()}
    }
  }
}