package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRBasicOperators}
import idb.query.{QueryEnvironment, NoColor}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRRemoteBasicOperators
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

			case (a, b) if a.color == NoColor && b.color != NoColor	=>
				crossProduct(
					relationA,
					remote(relationB, NoColor, relationB.color)
				)

			case (a, b) if a.color != NoColor && b.color == NoColor	=>
				crossProduct(
					remote(relationA, NoColor, relationA.color),
					relationB
				)

			case (a,b) if a.color != b.color =>
				crossProduct(
					remote(relationA, NoColor, relationA.color),
					remote(relationB, NoColor, relationB.color)
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

			case (a, b) if a.color == NoColor && b.color != NoColor	=>
				equiJoin(
					relationA,
					remote(relationB, NoColor, relationB.color),
					equalities
				)

			case (a, b) if a.color != NoColor && b.color == NoColor	=>
				equiJoin(
					remote(relationA, NoColor, relationA.color),
					relationB,
					equalities
				)

			case (a,b) if a.color != b.color =>
				//val thisDesc = relationA.remoteDesc union relationB.remoteDesc
				equiJoin(
					remote(relationA, NoColor, relationA.color),
					remote(relationB, NoColor, relationB.color),
					equalities
				)
			case _ => super.equiJoin(relationA, relationB, equalities)
		}
	}


}
