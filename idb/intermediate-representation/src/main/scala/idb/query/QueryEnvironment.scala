package idb.query

import akka.actor.ActorSystem
import idb.query.taint.TaintId

/**
 * This class describes the environment in which a query should be compiled to.
 *
 * @author Mirko Köhler
 */
trait QueryEnvironment {
	/**
	 * Gets the actor system for the environment.
	 */
	def system : ActorSystem

	/**
		Checks whether the environment is in a local or distributed setting. Local settings do not have to define an actor system.
	 */
	def isLocal : Boolean

	/**
	 * Returns a list of all hosts
	 */
	def hosts : Set[Host]

	/**
	 * Returns the access permissions of a host.
	 */
	def permissionsOf(host : Host) : Set[TaintId]

	/**
	  * Returns the priority of the host. Hosts with higher priority are preferred when distributing
	  * the query.
	  */
	def priorityOf(host : Host) : Int

	/**
	 * Closes the environment. Queries with that environment should no longer be used.
	 */
	def close(): Unit
}

protected class QueryEnvironmentImpl (
	val _system : Option[ActorSystem] = None,
	val _hosts : Map[Host, (Int, Set[TaintId])] = Map()
) extends QueryEnvironment {

	override def isLocal =
		_system.isEmpty

	override def system =
		if (_system.isDefined)
			_system.get
		else
			throw new UnsupportedByQueryEnvironmentException("No actor system", this)

	override def hosts =
		_hosts.keySet

	def permissionsOf(host : Host) : Set[TaintId] = _hosts.get(host) match {
		case Some(info) => info._2
		case _ => throw new NoSuchElementException(s"Host $host is not specified in the query environment.")
	}

	def priorityOf(host : Host) : Int = _hosts.get(host) match {
		case Some(info) => info._1
		case _ => throw new NoSuchElementException(s"Host $host is not specified in the query environment.")
	}

	override def close(): Unit = {
		if (_system.isDefined)
			_system.get.terminate()
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

	/**
	  * Creates a new query environment with the given data.
	  *
	  * @param system The actor system that should be used.
	  * @param hostInfo Provide information about the available hosts here. For each host you need to
	  *                 specify the priority as well as the permissions that should be used.
	  * @return A query environment with the given specifications.
	  */
	def create (
		system : ActorSystem = null,
		hostInfo : Map[Host, (Int, Set[String])] = Map()
	) : QueryEnvironment =
		new QueryEnvironmentImpl (
			_system = Option(system),
			_hosts = hostInfo.mapValues[(Int, Set[TaintId])](info =>
				(info._1, info._2.map(s => TaintId(s)))
			)

		)


}


