package sae.playground.remote.iql

import akka.remote.testkit.MultiNodeConfig

/**
  * @author Mirko Köhler
  */
object HospitalConfig extends MultiNodeConfig {

	val node1 = role("patient")
	val node2 = role("person")
	val node3 = role("knowledge")
	//val financeHost = role("finance")
}
