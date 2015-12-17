package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRBasicOperators}
import idb.query.{QueryEnvironment, DefaultDescription}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRDistBasicOperators
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRRemoteOperators{

	override def crossProduct[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		(relationA, relationB) match {
			case (Def(Remote(r, _, _)), _) => super.crossProduct(relationA, relationB)
			case (_, Def(Remote(r, _, _))) => super.crossProduct(relationA, relationB)

			case (a, b) if a.remoteDesc == DefaultDescription && b.remoteDesc != DefaultDescription	=>
				crossProduct(
					relationA,
					remote(relationB, DefaultDescription, relationB.remoteDesc)
				)

			case (a, b) if a.remoteDesc != DefaultDescription && b.remoteDesc == DefaultDescription	=>
				crossProduct(
					remote(relationA, DefaultDescription, relationA.remoteDesc),
					relationB
				)

			case (a,b) if a.remoteDesc != b.remoteDesc =>
				crossProduct(
					remote(relationA, DefaultDescription, relationA.remoteDesc),
					remote(relationB, DefaultDescription, relationB.remoteDesc)
				)
			case _ => super.crossProduct(relationA, relationB)
		}
	}

	override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]],
		equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] = {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB =  implicitly[Manifest[DomainB]]

		(relationA, relationB) match {
			case (Def(Remote(r, _, _)), _) => super.equiJoin(relationA, relationB, equalities)
			case (_, Def(Remote(r, _, _))) => super.equiJoin(relationA, relationB, equalities)

			case (a, b) if a.remoteDesc == DefaultDescription && b.remoteDesc != DefaultDescription	=>
				equiJoin(
					relationA,
					remote(relationB, DefaultDescription, relationB.remoteDesc),
					equalities
				)

			case (a, b) if a.remoteDesc != DefaultDescription && b.remoteDesc == DefaultDescription	=>
				equiJoin(
					remote(relationA, DefaultDescription, relationA.remoteDesc),
					relationB,
					equalities
				)

			case (a,b) if a.remoteDesc != b.remoteDesc =>
				//val thisDesc = relationA.remoteDesc union relationB.remoteDesc
				equiJoin(
					remote(relationA, DefaultDescription, relationA.remoteDesc),
					remote(relationB, DefaultDescription, relationB.remoteDesc),
					equalities
				)
			case _ => super.equiJoin(relationA, relationB, equalities)
		}
	}


}
