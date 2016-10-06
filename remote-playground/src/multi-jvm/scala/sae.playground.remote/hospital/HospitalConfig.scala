package sae.playground.remote.hospital

import akka.remote.testkit.MultiNodeConfig

/**
  * @author Mirko Köhler
  */
object HospitalConfig extends MultiNodeConfig {
	debugConfig(true)

	val node1 = role("patient")
	val node2 = role("person")
	val node3 = role("knowledge")
	val node4 = role("client")
}
