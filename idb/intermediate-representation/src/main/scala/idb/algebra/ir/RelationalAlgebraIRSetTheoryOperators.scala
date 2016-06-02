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

import idb.algebra.base.{RelationalAlgebraBasicOperators, RelationalAlgebraSetTheoryOperators}
import idb.algebra.exceptions.NonMatchingHostsException
import idb.query.colors.Color
import idb.query.QueryEnvironment


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRSetTheoryOperators
    extends RelationalAlgebraIRBase with RelationalAlgebraSetTheoryOperators
{

    case class UnionAdd[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        var relationA: Rep[Query[DomainA]],
        var relationB: Rep[Query[DomainB]]
    ) extends Def[Query[Range]] with QueryBaseOps {
        val mDomA = implicitly[Manifest[DomainA]]
        val mDomB = implicitly[Manifest[DomainB]]
        val mRan = implicitly[Manifest[Range]]

		def isMaterialized: Boolean = relationA.isMaterialized && relationB.isMaterialized
		def isSet = false
		def isIncrementLocal = relationA.isIncrementLocal && relationB.isIncrementLocal

		def color = relationA.color union relationB.color
		override def host = {
			if (relationA.host != relationB.host)
				throw new NonMatchingHostsException(relationA.host, relationB.host)
			relationA.host
		}

    }

    case class UnionMax[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        var relationA: Rep[Query[DomainA]],
        var relationB: Rep[Query[DomainB]]
    ) extends Def[Query[Range]] with QueryBaseOps {
        val mDomA = implicitly[Manifest[DomainA]]
        val mDomB = implicitly[Manifest[DomainB]]
        val mRan = implicitly[Manifest[Range]]

		def isMaterialized: Boolean = relationA.isMaterialized && relationB.isMaterialized && !isIncrementLocal
		def isSet = false
		def isIncrementLocal = relationA.isIncrementLocal && relationB.isIncrementLocal

		def color = relationA.color union relationB.color
		override def host = {
			if (relationA.host != relationB.host)
				throw new NonMatchingHostsException(relationA.host, relationB.host)
			relationA.host
		}
    }

    case class Intersection[Domain: Manifest] (
        var relationA: Rep[Query[Domain]],
        var relationB: Rep[Query[Domain]]
    ) extends Def[Query[Domain]] with QueryBaseOps {

		def isMaterialized: Boolean = relationA.isMaterialized && relationB.isMaterialized && !isIncrementLocal
		def isSet = false
		def isIncrementLocal = relationA.isIncrementLocal && relationB.isIncrementLocal

		def color = relationA.color union relationB.color
		override def host = {
			if (relationA.host != relationB.host)
				throw new NonMatchingHostsException(relationA.host, relationB.host)
			relationA.host
		}
	}

    case class Difference[Domain: Manifest] (
        var relationA: Rep[Query[Domain]],
        var relationB: Rep[Query[Domain]]
    ) extends Def[Query[Domain]] with QueryBaseOps {

		def isMaterialized: Boolean = !isIncrementLocal //Difference implements foreach
		def isSet = false
		def isIncrementLocal = relationA.isIncrementLocal && relationB.isIncrementLocal

		def color = relationA.color union relationB.color
		override def host = {
			if (relationA.host != relationB.host)
				throw new NonMatchingHostsException(relationA.host, relationB.host)
			relationA.host
		}
	}


    def unionAdd[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =
        UnionAdd (relationA, relationB)

    def unionMax[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =
        UnionMax (relationA, relationB)

    def intersection[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        Intersection (relationA, relationB)

    def difference[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        Difference (relationA, relationB)


}


