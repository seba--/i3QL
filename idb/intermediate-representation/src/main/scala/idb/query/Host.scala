package idb.query

import akka.actor._

/**
 * @author Mirko Köhler
 */
trait Host {
	def name : String
}

object Host {
	val local = LocalHost
	val unknown = UnknownHost
	def remote(name : String, address: Address) = RemoteHost(name, address)
}

case class RemoteHost(name : String, address: Address) extends Host

case class NamedHost(name : String) extends Host

case object LocalHost extends Host {
	val name = "localhost"
}

case object UnknownHost extends Host {
	val name = "unknown"
}

