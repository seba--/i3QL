package idb.query

import akka.actor.ActorSystem


/**
 * @author Mirko Köhler
 */
trait QueryContext {

	def actorSystem : ActorSystem
	def hosts : List[Host]
	def close(): Unit

}

class QueryContextImpl (
	val _actorSystem : Option[ActorSystem] = None,
    val _hosts : List[Host] = List()
) extends QueryContext {

	def actorSystem =
		if (_actorSystem.isDefined)
			_actorSystem.get
		else
			throw new UnsupportedByQueryContextException("No actor system", this)

	def hosts = _hosts

	def close(): Unit = {
		if (_actorSystem.isDefined)
			_actorSystem.get.shutdown()
	}
}

object QueryContext {

	val noRemote = create()

	def create(
		actorSystem : ActorSystem = null,
	    hosts : List[Host] = List()
	) : QueryContext =
		new QueryContextImpl (
			_actorSystem = if (actorSystem == null) None else Some(actorSystem),
			_hosts = hosts

		)


}


