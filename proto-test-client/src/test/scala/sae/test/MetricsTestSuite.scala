package sae.test

import org.junit.Assert._
import java.io.File
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.metrics.Metrics
import org.junit.{Ignore, Before, Test}
import de.tud.cs.st.bat.{Type, ObjectType, ReferenceType}

/**
 * Test class for the metric collection
 * @author Malte V
 */
class MetricsTestSuite extends org.scalatest.junit.JUnitSuite with AbstractEventSetTestSuite with MetricsTestCollection {
  val location = new File("./src/test/resources/MetricTestSet/")




  @Test
  def testFanOut() {
    val view = registerQuery(x => {
      Metrics.numberOfFanOutPerClass(x
      )
    })
    processRestAndThenTest({
      // test values are calculated by hand with javap after the definition of Fan out given in
      // Predicting Class Testability using Object-Oriented Metrics
      val res: QueryResult[(ReferenceType, Int)] = view
      res.foreach(println)
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Main$1"), 3)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/ResourceProvider"), 5)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Resource"), 7)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Consumer"), 15)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Main"), 11)))
    })

  }


  @Test
  def testLcom() {
    val view = registerQuery(x => {
      Metrics.LCOMStar(x)
    })
    processRestAndThenTest({
      // test values are calculated by hand with javap after the definition of LCOM* given in
      // Predicting Class Testability using Object-Oriented Metrics
      val res: QueryResult[(ReferenceType, Option[Double])] = view
      res.foreach(println)
      assertLCOM(res, ObjectType(className = "sharedresources/ResourceProvider"), 0.75)
      assertLCOM(res, ObjectType(className = "sharedresources/Resource"), 0.444)
      assertLCOM(res, ObjectType(className = "sharedresources/Consumer"), 0.5)
      assertTrue(!contains[ReferenceType, Option[Double]](res, ObjectType(className = "sharedresources/Main")))
      assertTrue(!contains[ReferenceType, Option[Double]](res, ObjectType(className = "sharedresources/Main$1")))
    })
  }

  @Test
  def testFanIn() {
    val view = registerQuery(x => {
      Metrics.numberOfFanInPerClass(x
      )
    })
    processRestAndThenTest({
      // test values are calculated by hand with javap
      val res: QueryResult[(Type, Int)] = view

      //assertTrue(res.asList.contains((ObjectType(className="sharedresources/Main"),0)))
      assertTrue(!contains[Type, Int](res, ObjectType(className = "sharedresources/Main")))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Main$1"), 1)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Consumer"), 1)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/ResourceProvider"), 2)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Resource"), 2)))
    })
  }

  @Test
  def testLcomMod() {
    val view = registerQuery(x => {
      Metrics.LCOMStarMod(x)
    })
    processRestAndThenTest({

      val res: QueryResult[(ReferenceType, Option[Double])] = view
      res.foreach(println)
      assertLCOM(res, ObjectType(className = "sharedresources/ResourceProvider"), 1)
      assertLCOM(res, ObjectType(className = "sharedresources/Resource"), 0.5)
      assertLCOM(res, ObjectType(className = "sharedresources/Consumer"), 1)
      assertTrue(!contains[ReferenceType, Option[Double]](res, ObjectType(className = "sharedresources/Main")))
      assertTrue(!contains[ReferenceType, Option[Double]](res, ObjectType(className = "sharedresources/Main$1")))
    })
  }

  @Test
  @Ignore
  def printData() {
    val view = registerQuery(x => {
      Metrics.numberOfFanOutPerClass(x
      )
    })
    processRestAndThenTest({})
    executeSomethingOnTheCurrentState(db => {
      db.classfile_fields.foreach(println _)
      db.classfile_methods.foreach(println _)
      db.read_field.foreach(println)
      db.write_field.foreach(println)
      db.calls.foreach(println)
    })
    view.foreach(println _)
  }


}


/**
 * A small trait with some assert for testing the lcom* metric
 * @author Malte V
 */
trait MetricsTestCollection {
  protected def assertDoIT(res: QueryResult[(ObjectType, Int)], objectType: ObjectType, value: Int) {
    assertTrue(res.asList.contains((objectType, value)))
  }

  protected def assertLCOM(res: QueryResult[(ReferenceType, Option[Double])], objectType: ObjectType, value: Double) {
    val dif = 0.001

    res.asList.foreach(x => {
      x match {
        case (o, Some(v)) => {
          if (o == objectType) {
            // println("value : " + v)
            assertTrue(value - dif <= v)
            assertTrue(v <= value + dif)
            return
          }
        }
        case _ =>
      }
    })
    fail()
  }

  protected def contains[Type, T](res: QueryResult[(Type, T)], objectType: Type): Boolean = {
    res.asList.foreach(x => {
      x match {
        case (o, _) => {

          if (o == objectType)
            return true
        }
        case _ =>
      }
    })

    false
  }
}

