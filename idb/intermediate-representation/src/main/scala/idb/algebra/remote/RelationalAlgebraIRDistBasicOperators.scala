package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRBasicOperators}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRDistBasicOperators
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRRemoteOperators{

	override def crossProduct[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]]
	): Rep[Query[(DomainA, DomainB)]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		(relationA, relationB) match {
			case (Def(Remote(r, _, _)), _) => super.crossProduct(relationA, relationB)
			case (_, Def(Remote(r, _, _))) => super.crossProduct(relationA, relationB)
			case (a,b) if a.remoteDesc != b.remoteDesc =>
				crossProduct(
					remote(relationA, relationA.remoteDesc, relationA.remoteDesc),
					remote(relationB, relationB.remoteDesc, relationB.remoteDesc)
				)
			case _ => super.crossProduct(relationA, relationB)
		}
	}

	override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]],
		equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
	): Rep[Query[(DomainA, DomainB)]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		(relationA, relationB) match {
			case (Def(Remote(r, _, _)), _) => super.equiJoin(relationA, relationB, equalities)
			case (_, Def(Remote(r, _, _))) => super.equiJoin(relationA, relationB, equalities)
			case (a,b) if a.remoteDesc != b.remoteDesc =>
				//val thisDesc = relationA.remoteDesc union relationB.remoteDesc
				equiJoin(
					remote(relationA, relationA.remoteDesc, relationA.remoteDesc),
					remote(relationB, relationB.remoteDesc, relationB.remoteDesc),
					equalities
				)
			case _ => super.equiJoin(relationA, relationB, equalities)
		}
	}


}
