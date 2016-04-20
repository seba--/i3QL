package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRBasicOperators}
import idb.query.colors.Color
import idb.query.{QueryEnvironment}
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

			case (a, b) if a.color == Color.NO_COLOR  && b.color != Color.NO_COLOR 	=>
				crossProduct(
					relationA,
					remote(relationB, Color.NO_COLOR , relationB.color)
				)

			case (a, b) if a.color != Color.NO_COLOR  && b.color == Color.NO_COLOR 	=>
				crossProduct(
					remote(relationA, Color.NO_COLOR , relationA.color),
					relationB
				)

			case (a,b) if a.color != b.color =>
				crossProduct(
					remote(relationA, Color.NO_COLOR , relationA.color),
					remote(relationB, Color.NO_COLOR , relationB.color)
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

			case (a, b) if a.color == Color.NO_COLOR  && b.color != Color.NO_COLOR 	=>
				equiJoin(
					relationA,
					remote(relationB, Color.NO_COLOR , relationB.color),
					equalities
				)

			case (a, b) if a.color != Color.NO_COLOR  && b.color == Color.NO_COLOR 	=>
				equiJoin(
					remote(relationA, Color.NO_COLOR , relationA.color),
					relationB,
					equalities
				)

			case (a,b) if a.color != b.color =>
				//val thisDesc = relationA.remoteDesc union relationB.remoteDesc
				equiJoin(
					remote(relationA, Color.NO_COLOR , relationA.color),
					remote(relationB, Color.NO_COLOR , relationB.color),
					equalities
				)
			case _ => super.equiJoin(relationA, relationB, equalities)
		}
	}


}
