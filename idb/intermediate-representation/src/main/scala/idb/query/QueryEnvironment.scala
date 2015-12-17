package idb.query

import akka.actor.ActorSystem


/**
 * This class describes the environment in which a query should be compiled to.
 *
 * @author Mirko Köhler
 */
trait QueryEnvironment {

	def actorSystem : ActorSystem
	def hosts : List[Host]
	def close(): Unit

}

protected class QueryEnvironmentImpl (
	val _actorSystem : Option[ActorSystem] = None,
    val _hosts : List[Host] = List()
) extends QueryEnvironment {

	def actorSystem =
		if (_actorSystem.isDefined)
			_actorSystem.get
		else
			throw new UnsupportedByQueryEnvironmentException("No actor system", this)

	def hosts = _hosts

	def close(): Unit = {
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
	    hosts : List[Host] = List()
	) : QueryEnvironment =
		new QueryEnvironmentImpl (
			_actorSystem = if (actorSystem == null) None else Some(actorSystem),
			_hosts = hosts

		)


}


