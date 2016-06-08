package idb.query

import akka.actor.ActorSystem
import idb.query.colors.{Color, ColorId, StringColor}

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
	def hosts : Set[Host]

	/**
	 * Maps the descriptions of tables (i.e. colors) to the hosts that have the right to read the tables.
	 */
	def permissionsOf(host : Host) : Set[ColorId]

	/*def permission(description : Color) : List[Host] = description match {
		case  => Nil
		case SingleColor(name) => permission(name)
		case CompoundColor(set) => set.map(desc => permission(desc)).fold(hosts)((a, b) => a intersect b)
	}    */

	/**
	 * Closes the environment. Queries with that environment should no longer be used.
	 */
	def close(): Unit
}

protected class QueryEnvironmentImpl (
	val _actorSystem : Option[ActorSystem] = None,
    val _permissions : Map[Host, Set[ColorId]] = Map()
) extends QueryEnvironment {

	override def actorSystem =
		if (_actorSystem.isDefined)
			_actorSystem.get
		else
			throw new UnsupportedByQueryEnvironmentException("No actor system", this)

	override def hosts =
		_permissions.keySet

	def permissionsOf(host : Host) : Set[ColorId] = _permissions.get(host) match {
		case Some(set) => set
		case _ => Set()
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

	/*def create(
		actorSystem : ActorSystem = null,
	    permissions : Map[Host, Set[ColorId]] = Map()
	) : QueryEnvironment =
		new QueryEnvironmentImpl (
			_actorSystem = Option(actorSystem),
		    _permissions = permissions
		)    */

	def create(
		actorSystem : ActorSystem = null,
		permissions : Map[Host, Set[String]] = Map()
	) : QueryEnvironment =
		new QueryEnvironmentImpl (
			_actorSystem = Option(actorSystem),
			_permissions = permissions.mapValues(setString => setString.map(s => StringColor(s)))
		)


}


