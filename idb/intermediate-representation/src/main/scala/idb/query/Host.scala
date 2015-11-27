package idb.query

/**
 * @author Mirko Köhler
 */
trait Host {
	def name : String
}

case class RemoteHost(name : String) extends Host

case object LocalHost extends Host {
	val name = "localhost"
}