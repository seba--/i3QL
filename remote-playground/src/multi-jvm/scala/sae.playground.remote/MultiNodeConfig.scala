package sae.playground.remote

object MultiNodeConfig extends akka.remote.testkit.MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
  val node3 = role("node3")
}


