package idb

import idb.query.taint.TaintId

package object query {

	def findHost(env : QueryEnvironment, hosts : Iterable[Host], taintIds : Set[TaintId]) : Option[Host] = {
		var bestHost : Host = null
		hosts.foreach(h => {
			import env._
			if (taintIds.subsetOf(permissionsOf(h)) && ( //The host needs to have the right permissions
				bestHost == null || //Either we do not have a best host yet
					priorityOf(bestHost) < priorityOf(h) || ( //or we have one with lower priority
					priorityOf(bestHost) == priorityOf(h) && permissionsOf(bestHost).size < permissionsOf(h).size //or we have one with same priority but lower rights
					)
				)) {
				bestHost = h
			}
		})

		Option(bestHost)
	}

	def findHost(env : QueryEnvironment, taintIds : Set[TaintId]) : Option[Host] = {
		findHost(env, env.hosts, taintIds)
	}

}
