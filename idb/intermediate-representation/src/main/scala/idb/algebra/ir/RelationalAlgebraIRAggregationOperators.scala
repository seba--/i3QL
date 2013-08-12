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

import idb.algebra.base.{RelationalAlgebraAggregationOperators, RelationalAlgebraBasicOperators}


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRAggregationOperators
    extends RelationalAlgebraIRBase with RelationalAlgebraAggregationOperators
{

    case class AggregationSelfMaintained[Domain: Manifest, Key: Manifest, AggregateValue: Manifest, Result: Manifest] (
        var relation: Rep[Query[Domain]],
        grouping: Rep[Domain => Key],
        added: Rep[Domain => AggregateValue],
        removed: Rep[Domain => AggregateValue],
        updated: Rep[((Domain, Domain)) => AggregateValue],
        convert: Rep[((Key, AggregateValue)) => Result]
    ) extends Def[Query[Result]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = false
	}

	case class AggregationSelfMaintainedWithoutGrouping[Domain : Manifest, Result : Manifest](
		relation : Rep[Query[Domain]],
		added : Rep[Domain => Result],
		removed : Rep[Domain => Result],
		updated: Rep[((Domain, Domain)) => Result]
	) extends Def[Query[Result]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = false
	}

	case class Grouping[Domain : Manifest, Result : Manifest] (
		relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Result]
	) extends Def[Query[Result]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = false
	}

    def aggregationSelfMaintained[Domain: Manifest, Key: Manifest, AggregateValue: Manifest, Result: Manifest] (
        relation: Rep[Query[Domain]],
        grouping: Rep[Domain => Key],
        added: Rep[Domain => AggregateValue],
        removed: Rep[Domain => AggregateValue],
        updated: Rep[((Domain, Domain)) => AggregateValue],
        convert: Rep[((Key, AggregateValue)) => Result]
    ): Rep[Query[Result]] =
        AggregationSelfMaintained (relation, grouping, added, removed, updated, convert)

	def aggregationSelfMaintainedWithoutGrouping[Domain : Manifest, Result : Manifest](
		relation : Rep[Query[Domain]],
		added : Rep[Domain => Result],
		removed : Rep[Domain => Result],
		updated: Rep[((Domain, Domain)) => Result]
	): Rep[Query[Result]] =
		AggregationSelfMaintainedWithoutGrouping (relation, added, removed, updated)

	def grouping[Domain : Manifest, Result : Manifest] (
		relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Result]
	): Rep[Query[Result]] =
		Grouping (relation,grouping)


}


