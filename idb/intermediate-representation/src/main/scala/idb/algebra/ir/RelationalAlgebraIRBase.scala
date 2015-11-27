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
import idb.annotations.{RemoteHost, LocalIncrement}
import idb.query.{QueryContext, DefaultDescription, RemoteDescription}
import scala.language.higherKinds
import scala.virtualization.lms.common.BaseExp
import scala.language.implicitConversions


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

		/**
		 * Indicates to which remote descriptions (colors) this node belongs to.
		 */
		def remoteDesc : RemoteDescription
    }

    abstract class QueryBase[+Domain: Manifest] extends QueryBaseOps

	implicit def repToQueryBaseOps(r : Rep[Query[_]]) : QueryBaseOps = {
		r match {
			case e : QueryBaseOps => e
			case Def ( d ) => d.asInstanceOf[QueryBaseOps]
		}
	}

    def domainOf[T] (relation: Rep[Query[T]]): Manifest[Any] =
        relation.tp.typeArguments (0).asInstanceOf[Manifest[Any]]

    def exactDomainOf[T] (relation: Rep[Query[T]]): Manifest[T] =
        relation.tp.typeArguments (0).asInstanceOf[Manifest[T]]


    case class QueryTable[Domain] (
        table: Table[Domain],
        isSet: Boolean = false,
        isIncrementLocal: Boolean = false,
        isMaterialized: Boolean = false,
	    remoteDesc : RemoteDescription = DefaultDescription
    )
            (implicit mDom: Manifest[Domain], mRel: Manifest[Table[Domain]])
        extends Exp[Query[Domain]] with QueryBaseOps

    case class QueryRelation[Domain] (
        table: Relation[Domain],
        isSet: Boolean = false,
        isIncrementLocal: Boolean = false,
        isMaterialized: Boolean = false,
		remoteDesc : RemoteDescription = DefaultDescription
    )
            (implicit mDom: Manifest[Domain], mRel: Manifest[Relation[Domain]])
        extends Exp[Query[Domain]] with QueryBaseOps

	case class Materialize[Domain : Manifest] (
	  relation : Rep[Query[Domain]]
	) extends Def[Query[Domain]] with QueryBaseOps {
		def isMaterialized: Boolean = true //Materialization is always materialized
		def isSet = false
		def isIncrementLocal = false
		def remoteDesc = relation.remoteDesc
	}

	case class Root[Domain : Manifest] (
		relation : Rep[Query[Domain]]
	) extends Def[Query[Domain]] with QueryBaseOps {
		def isMaterialized: Boolean = relation.isMaterialized
		def isSet = relation.isSet
		def isIncrementLocal = relation.isIncrementLocal
		def remoteDesc = DefaultDescription
	}

	//This version checks the type of the table for the annotation instead of the table itself
//    protected def isIncrementLocal[Domain] (m: Manifest[Domain]) = {
//		m.runtimeClass.getAnnotation (classOf[LocalIncrement]) != null
//	}

	protected def isIncrementLocal (m: Any) : Boolean = {
		m.getClass.getAnnotation(classOf[LocalIncrement]) != null
	}

	protected def getRemoteDescription (m : Any) : RemoteDescription = {
		val annotation = m.getClass.getAnnotation(classOf[RemoteHost])

		if (annotation == null)
			DefaultDescription
		else
			RemoteDescription(annotation.description())
	}

    /**
     * Wraps an table as a leaf in the query tree
     */
    override def table[Domain] (table: Table[Domain], isSet: Boolean = false, remote : RemoteDescription = DefaultDescription)(
        implicit mDom: Manifest[Domain],
        mRel: Manifest[Table[Domain]]
    ): Rep[Query[Domain]] =
        QueryTable (
			table,
			isSet = isSet,
			isIncrementLocal = isIncrementLocal (mDom),
			isMaterialized = false,
			if (remote == DefaultDescription) getRemoteDescription(table) else remote
		)


    /**
     * Wraps a compiled relation again as a leaf in the query tree
     */
    override def relation[Domain] (relation: Relation[Domain], isSet: Boolean = false)(
        implicit mDom: Manifest[Domain],
        mRel: Manifest[Relation[Domain]]
    ): Rep[Query[Domain]] =
		QueryRelation (
			relation,
			isSet = isSet,
			isIncrementLocal = isIncrementLocal (mDom),
			isMaterialized = false,
			getRemoteDescription(relation)
		)


	override def materialize[Domain : Manifest] (
		relation : Rep[Query[Domain]]
	)(implicit queryContext : QueryContext) : Rep[Query[Domain]] =
		Materialize(relation)



	override def root[Domain : Manifest] (
		relation : Rep[Query[Domain]]
	)(implicit queryContext : QueryContext): Rep[Query[Domain]] =
		Root(relation)


}
