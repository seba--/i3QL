package sae.test

import java.io.File
import sae.lyrebirdapi._
import java.lang.Long
import sae.LazyView
import org.junit.Before
import sae.bytecode.{Database, MaterializedDatabase, BytecodeDatabase}

/**
 * A suite which provides some methods to write junit test which use the lyrebird replay framework
 * This trait handles the update and initialisation stuff
 * A class that implements this trait must provide the location of the saved eventset (override val location : File)
 * You now can write registerQuery(..) to register a query on the internal handled database
 * after that you can use one of the prosses...AndThenTest( TESTCODE  ) to process X eventset and then execute your TESTCODE
 *
 * <pre>
 *
 * class TwoSuite extends  extends org.scalatest.junit.JUnitSuite with AbstractEventSetTestSuite {
 *   val location = new File("./src/test/resources/MetricTestSet/")
 *
 *   @Test
 *   def testFanIn() {
 *   val view = registerQuery(x => {   //registers the fan in metric
 *     Metrics.numberOfFanInPerClass(x
 *     )
 *   })
 *   processRestAndThenTest({          // processes all saved eventSet and then executes the code below
 *     val res: QueryResult[(Type, Int)] = view
 *     assertTrue(!contains[Type, Int](res, ObjectType(className = "sharedresources/Main")))
 *   })
 * }
 * </pre>
 *
 * @author Malte V
 */
trait AbstractEventSetTestSuite  {
  val location: File
  @Before
  def before() {
    init(location)
  }

  private var db: MaterializedDatabase = null
  private var lyrebird: LyrebirdRecorderAPI = null

  private def init(location: File) {
    db = new MaterializedDatabase()
    lyrebird = new LyrebirdRecorderAPI(location, db)
  }

  /**
   * Registers a query on the internal database
   * return: the registered query
   */
  def registerQuery[T <: AnyRef](query: Database => LazyView[T]): LazyView[T] = {
    query(db)
  }

  /**
   * Process the next eventset and then executes test
   * @param test : function that will be executed after processing the next lyrebird eventset.
   */
  def processNextEvenSetAndThenTest(test: => Unit) {
    if (lyrebird.hasNext) {
      lyrebird.processNext()
    } else {
      throw new Error()
    }
    test
  }

  /**
   * A debug method that allows to execute a function on the current database stat
   * e.g. print
   */
  def executeSomethingOnTheCurrentState(f : Database => Unit){
    f(db)
  }

  /**
   * Processes the next X lyrebird event set and then executes the test code
   */
  def processNextXEventSetAndThenTest(x: Int, test: Unit => _) {
    lyrebird.processNextX(x)
    test
  }

  /**
   * Processes all missing event sets and then executes the test code
   */
  def processRestAndThenTest(test:  => Unit) {
    while (lyrebird.hasNext) {
      lyrebird.processNext()
    }
    test
  }

  /**
   * Processes all eventsets with a timestamp <= time from the current position onwards
   * and then execute the test code
   */
  def processToAndThenTest(time: Long, test:  => Unit) {
    lyrebird.processUntil(time)
    test
  }
}

