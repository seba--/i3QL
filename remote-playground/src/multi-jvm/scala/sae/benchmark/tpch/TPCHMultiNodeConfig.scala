package sae.benchmark.tpch

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

/**
  * @author Mirko Köhler
  */
object TPCHMultiNodeConfig extends MultiNodeConfig {
	//debugConfig(true)

	commonConfig(ConfigFactory.parseString("""
		akka {
			actor {
				# Log level used by the configured loggers (see "loggers") as soon
				# as they have been started; before that, see "stdout-loglevel"
				# Options: OFF, ERROR, WARNING, INFO, DEBUG
				loglevel = "DEBUG"

				# Log level for the very basic logger activated during ActorSystem startup.
				# This logger prints the log messages to stdout (System.out).
				# Options: OFF, ERROR, WARNING, INFO, DEBUG
				stdout-loglevel = "DEBUG"



				warn-about-java-serializer-usage = false
			}
        }
    """))
	/*
	serializers {
		java = "akka.serialization.JavaSerializer"
	}

	# Change this setting to change the default serializer
		serialization-bindings {
			"idb.remote.DataMessage" = java
		}
		*/

	//					myown = "idb.remote.TestSerializer"


	//Data nodes
	val node_data_customer = role("node_data_customer")
	val node_data_nation = role("node_data_nation")
	val node_data_orders = role("node_data_orders")
	val node_data_part = role("node_data_part")
	val node_data_partsupp = role("node_data_partsupp")
	val node_data_region = role("node_data_region")
	val node_data_supplier = role("node_data_supplier")
	//processing nodes
	val node_process_finance = role("node_process_finance")
	val node_process_purchasing = role("node_process_purchasing")
	val node_process_shipping = role("node_process_shipping")
	val node_process_geographical = role("node_process_geographical")
	val node_process_private = role("node_process_private")
	//client node
	val node_client = role("node_client")
}
