package sae.benchmark

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Hooks up MultiNodeSpec with ScalaTest
  */
trait BenchmarkMultiNodeSpec extends MultiNodeSpecCallbacks
with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}

