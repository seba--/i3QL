package sae.benchmark.company

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

/**
  * @author Mirko Köhler
  */
object CompanyMultiNodeConfig extends MultiNodeConfig {
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
			}
        }
    """))

	val rolePublic = role("role:public")
	val roleProduction = role("role:production")
	val rolePurchasing = role("role:purchasing")
	val roleEmployees = role("role:employees")
	val roleClient = role("role:client")
	//val roleFinancial = role("domain:financial")
}
