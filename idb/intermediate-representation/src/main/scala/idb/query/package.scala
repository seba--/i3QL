package idb

import idb.query.colors.ColorId

/**
  * Created by mirko on 07.11.16.
  */
package object query {

	def findHost(env : QueryEnvironment, hosts : Iterable[Host], colorIds : Set[ColorId]) : Option[Host] = {
		var bestHost : Host = null
		hosts.foreach(h => {
			import env._
			if (colorIds.subsetOf(permissionsOf(h)) && ( //The host needs to have the right permissions
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

	def findHost(env : QueryEnvironment, colorIds : Set[ColorId]) : Option[Host] = {
		findHost(env, env.hosts, colorIds)
	}

}
