package idb.query

import akka.actor.ActorSystem

import scala.collection.mutable


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
	 * Maps the descriptions of tables (i.e. colors) to the hosts that have the right to read the tables.
	 */
	def permission(name : String) : List[Host]

	def permission(description : Color) : List[Host] = description match {
		case NoColor => Nil
		case SingleColor(name) => permission(name)
		case CompoundColor(set) => set.map(desc => permission(desc)).fold(hosts)((a, b) => a intersect b)
	}

	/**
	 * Closes the environment. Queries with that environment should no longer be used.
	 */
	def close(): Unit

	def colors : mutable.Map[Int, Set[Any]]

	def getColor(id : Int) : Set[Any] = {
		colors.get(id) match {
			case Some(c) => c
			case None => Set.empty
		}
	}

}

protected class QueryEnvironmentImpl (
	val _actorSystem : Option[ActorSystem] = None,
    val _hosts : List[Host] = List(),
    val _permissions : Map[String, List[Int]] = Map(),
 	val _colors : mutable.Map[Int, Set[Any]] = mutable.Map.empty[Int, Set[Any]]
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

	override def colors = _colors


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


