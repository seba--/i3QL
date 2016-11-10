package sae.benchmark.hospital

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

/**
  * @author Mirko Köhler
  */
object HospitalMultiNodeConfig extends MultiNodeConfig {
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

				serializers {
					java = "akka.serialization.JavaSerializer"
					proto = "akka.remote.serialization.ProtobufSerializer"
					myown = "idb.remote.TestSerializer"
				}

				# Change this setting to change the default serializer
				serialization-bindings {
					"idb.remote.DataMessage" = java
	            }

				warn-about-java-serializer-usage = false
			}
        }
    """))

	val node1 = role("patient")
	val node2 = role("person")
	val node3 = role("knowledge")
	val node4 = role("client")
}
