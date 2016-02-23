package sae.playground.remote

import akka.remote.testkit.MultiNodeConfig

object MultiNodeConfig extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
  val node3 = role("node3")
}


