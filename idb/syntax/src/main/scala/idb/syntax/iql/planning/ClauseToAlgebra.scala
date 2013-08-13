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
package idb.syntax.iql.planning

//import idb.syntax.iql.IR._

import idb.syntax.iql._
import idb.syntax.iql.impl._

/**
 *
 * @author Ralf Mitschke
 *
 */

object ClauseToAlgebra
    extends PlanSelectClause
    with PlanWhereClause
{

    override val IR = idb.syntax.iql.IR

    import IR._

    def apply[Select: Manifest, Domain <: Select: Manifest, Range: Manifest] (
        query: IQL_QUERY_1[Select, Domain, Range]
    ): Rep[Query[Range]] =
        query match {

			case FromClause1 (relation, SelectClause1 (ProjectionFunction1 (project), asDistinct)) =>
				distinct (projection (relation, project), asDistinct)

			case WhereClause1 (predicate, FromClause1 (relation, SelectClause1 (ProjectionFunction1 (project), asDistinct))) =>
				distinct (projection (selection (relation, predicate), project), asDistinct)

			case GroupByClause1 (group, FromClause1 (relation, SelectClause1 (ProjectionFunction1 (project), asDistinct))) =>
				distinct (projection (grouping (relation, group), project), asDistinct)

			case GroupByClause1 (group, WhereClause1 (predicate, FromClause1 (relation, SelectClause1 (ProjectionFunction1 (project), asDistinct)))) =>
				distinct (projection (grouping (selection (relation, predicate), group), project), asDistinct)

       }

    def apply[SelectA: Manifest, SelectB: Manifest, DomainA <: SelectA: Manifest, DomainB <: SelectB: Manifest, Range: Manifest] (
        query: IQL_QUERY_2[SelectA, SelectB, DomainA, DomainB, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause2 (relationA, relationB, SelectClause2 (project, asDistinct)) =>
                distinct (projection (crossProduct (relationA, relationB), project), asDistinct)

            case WhereClause2 (predicate, FromClause2 (relationA, relationB, SelectClause2 (project, asDistinct))) =>
				distinct (projection (selection (crossProduct (relationA, relationB), predicate), project), asDistinct)

			case GroupByClause2 (group, FromSelect1Clause2 (relationA, relationB, SelectClause1 (ProjectionFunction1 (project), asDistinct))) =>
				distinct (projection (grouping (crossProduct (relationA, relationB), group), project), asDistinct)

			case GroupByClause2 (group, GroupedWhereClause2 (predicate, FromSelect1Clause2 (relationA, relationB, SelectClause1 (ProjectionFunction1 (project), asDistinct)))) =>
				distinct (projection (grouping (selection (crossProduct (relationA, relationB), predicate), group), project), asDistinct)


            }


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Range: Manifest] (
        query: IQL_QUERY_3[DomainA, DomainB, DomainC, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause3 (relationA, relationB, relationC, select@SelectClause3 (_, _)) =>
                transform (select, crossProduct (relationA, relationB, relationC))

            case where@WhereClause3 (_, FromClause3 (relationA, relationB, relationC, select@SelectClause3 (_, _))) => {
                transform (select, transform (where, relationA, relationB, relationC))
            }
        }

    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        query: IQL_QUERY_4[DomainA, DomainB, DomainC, DomainD, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause4 (relationA, relationB, relationC, relationD, select@SelectClause4 (_, _)) =>
                transform (select, crossProduct (relationA, relationB, relationC, relationD))

            case where@WhereClause4 (_,
            FromClause4 (relationA, relationB, relationC, relationD, select@SelectClause4 (_, _))) =>
                transform (select, transform (where, relationA, relationB, relationC, relationD))
        }

    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        query: IQL_QUERY_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause5 (relationA, relationB, relationC, relationD, relationE, select@SelectClause5 (_, _)) =>
                transform (select, crossProduct (relationA, relationB, relationC, relationD, relationE))

            case where@WhereClause5 (_,
            FromClause5 (relationA, relationB, relationC, relationD, relationE, select@SelectClause5 (_, _))
            ) =>
                transform (select, transform (where, relationA, relationB, relationC, relationD, relationE))
        }

	private def distinct[Domain : Manifest](query : Rep[Query[Domain]], asDistinct : Boolean) : Rep[Query[Domain]] = {
		asDistinct match {
			case true => duplicateElimination(query)
			case false => query
		}
	}


}
