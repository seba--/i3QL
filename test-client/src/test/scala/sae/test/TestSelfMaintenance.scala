package sae.test

import org.junit.Assert._
import java.io.File
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.metrics.Metrics
import org.junit.{Ignore, Before, Test}
import de.tud.cs.st.bat.{Type, ObjectType, ReferenceType}
import sae.operators.HashEquiJoin
import sae.operators.Conversions._
import sae.{Observer, BagExtent, Relation}


/**
 * Date: 01.07.11
 * Time: 13:53
 * @author Malte V
 */
// TODO Delete me
class TestSelfMaintenance extends org.scalatest.junit.JUnitSuite {

  class TestObs[A <: AnyRef] extends Observer[A]() {
    var i = 0

    def updated(oldV: A, newV: A): Unit = {
      //println("update " + oldV + newV)
    }

    def removed(v: A): Unit = {
      i -= 1
      println("remove " + v)
    }

    def added(v: A): Unit = {
      i += 1
      println("add " + v)

    }
  }

  //@Ignore // moved to StudentCourse Example
  @Test
  def elementOf(): Unit = {


/*
    val w1: Relation[String] = new BagExtent[String]()
    val w2: Relation[String] = new BagExtent[String]()


    val elementOf = ∈(w2)
    val select = σ(elementOf)(w1)

    val testObs = new TestObs[String]()
    select.addObserver(testObs)

    val key = "aKey"
    w1.element_added(key)
    w1.element_added(key)
    w1.element_added(key)

    w2.element_added(key)
    w2.element_added(key)

    w1.element_removed(key)
    w1.element_removed(key)
    w1.element_removed(key)

    if(testObs.i < 0) fail()
*/

  }

 //@Ignore // moved to StudentCourse Example
 @Test
  def intersection(): Unit = {
    case class SimpleClass(val key: String, val data: String)

    val w1: Relation[String] = new BagExtent[String]()
    val w2: Relation[String] = new BagExtent[String]()
    val intersection = w1 ∩ w2

    val testObs = new TestObs[String]()
    intersection.addObserver(testObs)

    val key = "aKey"
    w1.element_added(key)
    w1.element_added(key)
    w1.element_added(key)

    w2.element_added(key)
    w2.element_added(key)

    w1.element_removed(key)
    w1.element_removed(key)
    w1.element_removed(key)

    if(testObs.i < 0) fail()

  }

}