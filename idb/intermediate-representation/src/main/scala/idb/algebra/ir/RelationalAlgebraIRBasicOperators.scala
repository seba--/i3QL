/* LiceUnionBSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.algebra.ir

import idb.algebra.base.RelationalAlgebraBasicOperators
import idb.algebra.exceptions.NonMatchingHostsException
import idb.lms.extensions.{FunctionUtils, RemoteUtils}
import idb.query.{Host, QueryEnvironment}
import idb.query.colors.{ClassColor, Color, FieldColor, FieldName}


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRBasicOperators
    extends RelationalAlgebraIRBase
		with RelationalAlgebraBasicOperators
		with RemoteUtils
{

    case class Projection[Domain : Manifest, Range : Manifest] (
        var relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    ) extends Def[Query[Range]] with QueryBaseOps {
		val mDom = implicitly[Manifest[Domain]]
		val mRan = implicitly[Manifest[Domain]]

		override def isMaterialized: Boolean = relation.isMaterialized
		override def isSet = false
		override def isIncrementLocal = relation.isIncrementLocal

		override def color = projectionColor(relation.color, function)
		override def host = relation.host
	}

    case class Selection[Domain: Manifest] (
        var relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ) extends Def[Query[Domain]] with QueryBaseOps {
        val mDom = implicitly[Manifest[Domain]]

		override def isMaterialized: Boolean = relation.isMaterialized
		override def isSet = false
		override def isIncrementLocal = relation.isIncrementLocal

		override def color =
			Color.union(relation.color, Color.fromIdsInColors(colorsOfTFields(function, relation.color)))
		override def host = relation.host
	}

    case class CrossProduct[DomainA: Manifest, DomainB: Manifest] (
        var relationA: Rep[Query[DomainA]],
        var relationB: Rep[Query[DomainB]]
    ) extends Def[Query[(DomainA, DomainB)]] with QueryBaseOps
    {
        val mDomA = implicitly[Manifest[DomainA]]
        val mDomB = implicitly[Manifest[DomainB]]

		override def isMaterialized: Boolean = relationA.isMaterialized && relationB.isMaterialized && !isIncrementLocal
		override def isSet = false
		override def isIncrementLocal = relationA.isIncrementLocal && relationB.isIncrementLocal

		override def color = Color.tupled(relationA.color, relationB.color)

		override def host = {
			if (relationA.host != relationB.host)
				throw new NonMatchingHostsException(relationA.host, relationB.host)
			relationA.host
		}
    }

    case class EquiJoin[DomainA: Manifest, DomainB: Manifest] (
        var relationA: Rep[Query[DomainA]],
        var relationB: Rep[Query[DomainB]],
        equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
	) extends Def[Query[(DomainA, DomainB)]] with QueryBaseOps {

		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB = implicitly[Manifest[DomainB]]

		override def isMaterialized: Boolean = relationA.isMaterialized && relationB.isMaterialized && !isIncrementLocal
		override def isSet = false
		override def isIncrementLocal = relationA.isIncrementLocal && relationB.isIncrementLocal

		override def color = {
			val e = equalities.foldRight(Predef.Set.empty[Color])((a, b) => colorsOfTFields(a._1, relationA.color))
			val f = equalities.foldRight(Predef.Set.empty[Color])((a, b) => colorsOfTFields(a._2, relationB.color))

			val eUnionF = Color.union(Color.fromIdsInColors(e), Color.fromIdsInColors(f))
			Color.tupled(Color.union(relationA.color, eUnionF), Color.union(relationB.color, eUnionF))
		}

		override def host = {
			if (relationA.host != relationB.host)
				throw new NonMatchingHostsException(relationA.host, relationB.host)
			relationA.host
		}
	}

    case class DuplicateElimination[Domain: Manifest] (
        var relation: Rep[Query[Domain]]
    ) extends Def[Query[Domain]] with QueryBaseOps {

		override def isMaterialized: Boolean = !isIncrementLocal //Duplicate Elimination stores intermediate objects and therefore implements foreach
		override def isSet = false
		override def isIncrementLocal = relation.isIncrementLocal

		override def color = relation.color
		override def host = relation.host

	}

    case class Unnest[Domain: Manifest, Range: Manifest] (
        var relation: Rep[Query[Domain]],
        unnesting: Rep[Domain => Traversable[Range]]
	) extends Def[Query[(Domain,Range)]] with QueryBaseOps {

		override def isMaterialized: Boolean = relation.isMaterialized
		override def isSet = false
		override def isIncrementLocal = relation.isIncrementLocal

		override def color = Color.tupled(relation.color, Color.fromIdsInColors(colorsOfTFields(unnesting, relation.color)))
		override def host = relation.host
	}


    override def projection[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =
		Projection (relation, function)


	override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
		Selection (relation, function)
	}




	override def crossProduct[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] =
		CrossProduct (relationA, relationB)




	override def equiJoin[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        equalities: List[(Rep[DomainA => Any], Rep[DomainB => Any])]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB)]] =
		EquiJoin (relationA, relationB, equalities)


	override def duplicateElimination[Domain: Manifest] (
        relation: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
		DuplicateElimination (relation)



	override def unnest[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        unnesting: Rep[Domain => Traversable[Range]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[(Domain, Range)]] =
	   Unnest (relation, unnesting)



}


