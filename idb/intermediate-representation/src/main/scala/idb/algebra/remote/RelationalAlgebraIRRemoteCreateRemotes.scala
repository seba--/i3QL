package idb.algebra.remote

import idb.algebra.exceptions.{InsufficientRootPermissionsException, NoServerAvailableException, NonMatchingHostsException}
import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}
import idb.lms.extensions.ColorUtils
import idb.query.colors.{Color, ColorId}
import idb.query.{Host, QueryEnvironment}

import scala.collection.mutable
/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRRemoteCreateRemotes
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRRemoteOperators
	with ColorUtils
{

	override def root[Domain : Manifest] (
		relation : Rep[Query[Domain]],
		host : Host
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
		//Adds a remote node as child of the root if the relation is on another server
		if (relation.host != host) {
			val rootPermissions = queryEnvironment.permissionsOf(host)

			if (relation.color.ids subsetOf rootPermissions)
				root(remote(relation, host), host)
			else
				throw new InsufficientRootPermissionsException(host.name, rootPermissions, relation.color)
		} else
			super.root(relation, host)
	}

	override def reclassification[Domain : Manifest] (
		relation : Rep[Query[Domain]],
		newColor : Color
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
		val host = relation.host

		if (newColor.ids subsetOf queryEnvironment.permissionsOf(host))
			super.reclassification(relation, newColor)
		else {
			val h = idb.query.findHost(queryEnvironment, newColor.ids)
			h match {
				case Some(x) =>
					super.reclassification(remote(relation, x), newColor)
				case None =>
					throw new NoServerAvailableException(newColor.ids)
			}
		}
	}

	override def crossProduct[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
		distributeRelations[DomainA, DomainB, (DomainA, DomainB)](
			relationA, relationB, (a, b) => super.crossProduct(a, b))
	}

	override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]],
		equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
		distributeRelations[DomainA, DomainB, (DomainA, DomainB)](
			relationA, relationB, (a, b) => super.equiJoin(a, b, equalities))
	}

	//TODO Add set theory operators

}
