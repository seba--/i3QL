package sae.test

import sae.SetExtent
import sae.operators.impl.TransactionalFixPointRecursionView
import org.junit.{Assert, Test}
import org.junit.Assert._
import collection.immutable.HashSet

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 27.01.13
 * Time: 22:28
 * To change this template use File | Settings | File Templates.
 */
class TestTransactionalRecursionView {

  case class Edge(from : Int, to : Int) {

  }

  @Test
  def testRecursion1() {

    val edges = new SetExtent[Edge]()
    val res = sae.relationToResult (
      //RecursionView to compute the possible paths (e.g. set of nodes traversed) to a node
      new TransactionalFixPointRecursionView[Edge,(Int,Set[Int]),Int](edges,
        edge => if (edge.from == 1) Some((1,new HashSet[Int])) else None,
        edge => edge.from,
        range => range._1,
        (edge,range) => (edge.to, range._2 + edge.from)
      )
    )

    edges.element_added(Edge(1,2))
    edges.element_added(Edge(2,3))
    edges.element_added(Edge(3,2))
    edges.element_added(Edge(2,4))
    edges.notifyEndTransaction()

    val resList = res.asList
    println(resList)

    Assert.assertEquals(7,resList.size)

    Assert.assertTrue(resList.contains((1,new HashSet[Int])))
    Assert.assertTrue(resList.contains((2,new HashSet[Int] + 1)))
    Assert.assertTrue(resList.contains((2,new HashSet[Int] + 1 + 2 + 3)))
    Assert.assertTrue(resList.contains((3,new HashSet[Int] + 1 + 2)))
    Assert.assertTrue(resList.contains((3,new HashSet[Int] + 1 + 2 + 3)))
    Assert.assertTrue(resList.contains((4,new HashSet[Int] + 1 + 2)))
    Assert.assertTrue(resList.contains((4,new HashSet[Int] + 1 + 2 + 3)))


  }

  @Test
  def testRecursion2() {

    val edges = new SetExtent[Edge]()
    val res = sae.relationToResult (
      //RecursionView to compute the possible paths (e.g. set of nodes traversed) to a node
      new TransactionalFixPointRecursionView[Edge,(Int,Set[Int]),Int](edges,
        edge => if (edge.from == 1) Some((1,new HashSet[Int])) else None,
        edge => edge.from,
        range => range._1,
        (edge,range) => (edge.to, range._2 + edge.from)
      )
    )


    edges.element_added(Edge(1,2))
    edges.element_added(Edge(1,3))
    edges.element_added(Edge(2,5))
    edges.element_added(Edge(3,4))
    edges.element_added(Edge(4,6))
    edges.element_added(Edge(5,1))
    edges.element_added(Edge(5,6))
    edges.notifyEndTransaction()

    val resList = res.asList
    println(resList)

    Assert.assertEquals(13,resList.size)

    Assert.assertTrue(resList.contains((1,new HashSet[Int])))
    Assert.assertTrue(resList.contains((1,new HashSet[Int] + 1 + 2 + 5)))
    Assert.assertTrue(resList.contains((6,new HashSet[Int] + 1 + 2 + 5)))
    Assert.assertTrue(resList.contains((6,new HashSet[Int] + 1 + 3 + 4)))
    Assert.assertTrue(resList.contains((6,new HashSet[Int] + 1 + 2 + 3 + 4 + 5)))
    Assert.assertTrue(resList.contains((3,new HashSet[Int] + 1 + 2 + 5)))
    Assert.assertTrue(resList.contains((3,new HashSet[Int] + 1)))


  }

}
