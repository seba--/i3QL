package sae.playground.remote

import java.io.File

import com.typesafe.config.ConfigFactory


object MultiNodeConfig extends akka.remote.testkit.MultiNodeConfig {
	val node1 = role("node1")
	val node2 = role("node2")
}


