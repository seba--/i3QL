package sae.test

import java.io.File
import org.junit.Before

/**
 * Date: 20.06.11
 * Time: 16:57
 * @author Malte V
 */

trait AbstractEventSetTestSuite {
  val location: File
  val helper = new EventSetTestHelper()

  import helper._

  @Before
  def before() {
    init(location)
  }
}