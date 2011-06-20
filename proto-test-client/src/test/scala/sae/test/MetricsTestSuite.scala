package sae.test

/**
 * Date: 16.06.11
 * Time: 00:23
 * @author Malte V
 */

import org.junit.Assert._
import java.io.File
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.LazyView
import sae.metrics.Metrics
import org.junit.{Ignore, Before, Test}
import javax.xml.crypto.dsig.Reference
import de.tud.cs.st.bat.{Type, ObjectType, ReferenceType}


trait AbstractEventSetTestSuite {
  val location: File
  val helper = new EventSetTestHelper()

  import helper._

  @Before
  def before() {
    init(location)
  }
}

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

class MetricsTestSuite extends org.scalatest.junit.JUnitSuite with AbstractEventSetTestSuite with MetricsTestCollection {
  val location = new File("./src/test/resources/MetricTestSet/")

  import helper._


  @Test
  def testFanOut() {
    val view = registerQuery(x => {
      Metrics.getFanOut(x.parameter,
        x.classfile_methods,
        x.read_field,
        x.write_field,
        x.classfile_fields,
        x.calls
      )
    })
    processRestAndTest({
      // test values are calculated by hand with javap after the definition of Fan out given in
      // Predicting Class Testability using Object-Oriented Metrics
      val res: QueryResult[(ReferenceType, Int)] = view
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Main$1"), 3)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/ResourceProvider"), 5)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Resource"), 7)))
      assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Consumer"), 15)))
      //TODO put back after fanout to exceptions is counted
      //assertTrue(res.asList.contains((ObjectType(className = "sharedresources/Main"), 11)))
    })

  }

  @Test
  def testLcom() {
    val view = registerQuery(x => {
      Metrics.getLCOMStar(x.read_field, x.write_field, x.classfile_methods, x.classfile_fields)
    })
    processRestAndTest({
      // test values are calculated by hand with javap after the definition of LCOM* given in
      // Predicting Class Testability using Object-Oriented Metrics
      val res: QueryResult[(ReferenceType, Option[Double])] = view
      assertLCOM(res, ObjectType(className = "sharedresources/ResourceProvider"), 0.75)
      assertLCOM(res, ObjectType(className = "sharedresources/Resource"), 0.444)
      assertLCOM(res, ObjectType(className = "sharedresources/Consumer"), 0.3333)
      assertTrue(!contains[ReferenceType, Option[Double]](res, ObjectType(className = "sharedresources/Main")))
      assertTrue(!contains[ReferenceType, Option[Double]](res, ObjectType(className = "sharedresources/Main$1")))
    })
  }

  @Test
  def testFanIn() {
    val view = registerQuery(x => {
      Metrics.getFanIn(x.parameter,
        x.classfile_methods,
        x.read_field,
        x.write_field,
        x.classfile_fields,
        x.calls
      )
    })
    processRestAndTest({
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
  def printData() {
    val view = registerQuery(x => {
      Metrics.getFanOut(x.parameter,
        x.classfile_methods,
        x.read_field,
        x.write_field,
        x.classfile_fields,
        x.calls
      )
    })
    processRestAndTest({})
    print(db => {
      db.classfile_fields.foreach(println _)
      db.classfile_methods.foreach(println _)
      db.read_field.foreach(println)
      db.write_field.foreach(println)
      db.calls.foreach(println)
    })
    //Main must have a fan out of 11
    //Lcom* of 0.5 (we cont static final vars)
    //some static final vars are converted to "real" constants like "a cons" or "4"
    //=> we may dont get all

    //Main$1
    //Fanout 3
    //LCOm* 0

    //Consumer
    //Fan out 15
    //Lcom* 1/3

    //Resouce:
    //fanout 7
    //lcom 4/9


    view.foreach(println _)
  }


}

