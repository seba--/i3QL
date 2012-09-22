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
package sae.syntax.sql.impl

import sae.syntax.sql.SELECT_CLAUSE_AGGREGATION_NO_PROJECTION
import sae.Relation
import sae.syntax.sql.ast.{AggregateSelectClauseSelfMaintainable2, AggregateSelectClauseSelfMaintainable1}
import sae.operators.SelfMaintainableAggregateFunctionFactory

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 02.09.12
 * Time: 19:38
 */
case class SelectClauseAggregationSelfMaintainableNoProjectionSyntax[AggregateValue, Result <: AnyRef](functionFactory: SelfMaintainableAggregateFunctionFactory[AnyRef, AggregateValue],
                                                                                                       distinct: Boolean = false)
    extends SELECT_CLAUSE_AGGREGATION_NO_PROJECTION[Result]
{
    def FROM[Domain <: AnyRef](relation: Relation[Domain]) =
        FromClause1Syntax[Domain, Result](
            AggregateSelectClauseSelfMaintainable1[Domain, Domain, AggregateValue, Result](
                None,
                functionFactory.asInstanceOf[SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]],
                distinct
            ),
            relation
        )

    def FROM[DomainA <: AnyRef, DomainB <: AnyRef](relationA: Relation[DomainA], relationB: Relation[DomainB]) =
        FromClause2Syntax[DomainA, DomainB, Result](
            AggregateSelectClauseSelfMaintainable2[DomainA, DomainB, (DomainA, DomainB), AggregateValue, Result](
                None,
                functionFactory.asInstanceOf[SelfMaintainableAggregateFunctionFactory[(DomainA, DomainB), AggregateValue]],
                distinct
            ),
            relationA,
            relationB
        )
}
