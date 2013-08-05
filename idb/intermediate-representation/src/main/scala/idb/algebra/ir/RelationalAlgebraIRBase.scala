/* License (BSD Style License):
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

import idb.algebra.base.RelationalAlgebraBase
import idb.annotations.LocalIncrement
import scala.language.higherKinds
import scala.virtualization.lms.common.BaseExp


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRBase
    extends RelationalAlgebraBase
    with BaseExp
{
    type Query[Domain] = QueryBase[Domain]

    trait QueryBaseOps
    {

        def isSet: Boolean

        def isIncrementLocal: Boolean

        /**
         * A query is materialized, if the elements of the underlying relation are stored and can be accessed by
         * foreach.
         * @return True, if the query is materialized.
         */
        def isMaterialized: Boolean
    }

    abstract class QueryBase[Domain: Manifest] extends QueryBaseOps

    def domainOf[T] (relation: Rep[Query[T]]): Manifest[Any] =
        relation.tp.typeArguments (0).asInstanceOf[Manifest[Any]]

    case class QueryExtent[Domain] (
        extent: Extent[Domain],
        isSet: Boolean = false,
        isIncrementLocal: Boolean = false,
        isMaterialized: Boolean = false
    )
            (implicit mDom: Manifest[Domain], mRel: Manifest[Extent[Domain]])
        extends Exp[Query[Domain]] with QueryBaseOps

    case class QueryRelation[Domain] (
        extent: Relation[Domain],
        isSet: Boolean = false,
        isIncrementLocal: Boolean = false,
        isMaterialized: Boolean = false
    )
            (implicit mDom: Manifest[Domain], mRel: Manifest[Relation[Domain]])
        extends Exp[Query[Domain]] with QueryBaseOps

	case class Materialize[Domain : Manifest] (
	  relation : Rep[Query[Domain]]
	) extends Def[Query[Domain]] with QueryBaseOps {
		def isMaterialized: Boolean = true //Materialization is always materialized
		def isSet = false
		def isIncrementLocal = false
	}


    protected def isIncrementLocal[Domain] (m: Manifest[Domain]) = {
        m.runtimeClass.getAnnotation (classOf[LocalIncrement]) != null
    }

    /**
     * Wraps an extent as a leaf in the query tree
     */
    override def extent[Domain] (extent: Extent[Domain], isSet: Boolean = false)(
        implicit mDom: Manifest[Domain],
        mRel: Manifest[Extent[Domain]]
    ): Rep[Query[Domain]] =
        QueryExtent (extent, isSet, isIncrementLocal (mDom))


    /**
     * Wraps a compiled relation again as a leaf in the query tree
     */
    override def relation[Domain] (relation: Relation[Domain], isSet: Boolean = false)(
        implicit mDom: Manifest[Domain],
        mRel: Manifest[Relation[Domain]]
    ): Rep[Query[Domain]] =
        QueryRelation (relation, isSet, isIncrementLocal (mDom))

<<<<<<< HEAD
	override def materialize[Domain : Manifest] (
		relation : Rep[Query[Domain]]
	): Rep[Query[Domain]] =
		Materialize(relation)

	implicit def repToQueryBaseOps(r : Rep[Query[_]]) : QueryBaseOps = {
		r match {
			case e : QueryBaseOps => e
			case Def (r) => r.asInstanceOf[QueryBaseOps]
		}
	}
=======
>>>>>>> 7fad27133669fde4db6b0399be681b856a00e43b

}
