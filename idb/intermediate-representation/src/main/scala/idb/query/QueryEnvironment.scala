package idb.query

import akka.actor.ActorSystem


/**
 * This class describes the environment in which a query should be compiled to.
 *
 * @author Mirko Köhler
 */
trait QueryEnvironment {
	/**
	 * Gets the actor system for the environment.
	 */
	def actorSystem : ActorSystem

	/**
	 * Returns a list of all hosts
	 */
	def hosts : List[Host]

	/**
	 * Maps the descriptions of tables (i.e. colors) to the hosts that have the right to read the tables. The hosts are specified by their index in the host list.
	 */
	def permission(name : String) : List[Host]

	/**
	 * Closes the environment. Queries with that environment should no longer be used.
	 */
	def close(): Unit

}

protected class QueryEnvironmentImpl (
	val _actorSystem : Option[ActorSystem] = None,
    val _hosts : List[Host] = List(),
    val _permissions : Map[String, List[Int]] = Map()
) extends QueryEnvironment {

	override def actorSystem =
		if (_actorSystem.isDefined)
			_actorSystem.get
		else
			throw new UnsupportedByQueryEnvironmentException("No actor system", this)

	override def hosts = _hosts

	override def permission(name : String) = _permissions.get(name) match {
		case Some(l) => l.map(i => _hosts(i))
		case _ => List()
	}

	override def close(): Unit = {
		if (_actorSystem.isDefined)
			_actorSystem.get.shutdown()
	}
}

object QueryEnvironment {

	/**
	 * Environment used for local queries.
	 */
	val Local = create()

	/**
	 * Default environment, when no other environment has been specified.
	 */
	val Default = Local

	def create(
		actorSystem : ActorSystem = null,
	    hosts : List[Host] = List(),
	    permissions : Map[String, List[Int]] = Map()
	) : QueryEnvironment =
		new QueryEnvironmentImpl (
			_actorSystem = if (actorSystem == null) None else Some(actorSystem),
			_hosts = hosts,
		    _permissions = permissions
		)


}


