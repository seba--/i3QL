package idb.query

/**
 * @author Mirko Köhler
 */
trait Host {
	def name : String
}

object Host {
	val local = LocalHost
	def remote(name : String) = RemoteHost(name)
}

case class RemoteHost(name : String) extends Host

case object LocalHost extends Host {
	val name = "localhost"
}

case object UnknownHost extends Host {
	val name = "unknown"
}