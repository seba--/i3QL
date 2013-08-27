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

import idb.syntax.iql.impl._
import idb.syntax.iql.impl.SelectClause3
import idb.syntax.iql.impl.SelectClause4
import idb.syntax.iql.impl.SelectClause1
import idb.syntax.iql.impl.SelectClause2

/**
 *
 * @author Ralf Mitschke
 *
 */

trait PlanSelectClause
{

    val IR = idb.syntax.iql.IR

    import IR.Rep
    import IR.Query
    import IR.projection
    import IR.duplicateElimination

/*    def transform[Select: Manifest, Domain <: Select : Manifest, Range: Manifest] (
        clause: SelectClause1[Select, Range],
        relation: Rep[Query[Domain]]
    ): Rep[Query[Range]] =
        if (!clause.asDistinct) {
            projection (relation, clause.function)
        }
        else
        {
            duplicateElimination (projection (relation, clause.function))
        }


    def transform[SelectA: Manifest, SelectB: Manifest, DomainA <: SelectA : Manifest, DomainB <: SelectB : Manifest,
    Range: Manifest] (
        clause: SelectClause2[SelectA, SelectB, Range],
        relation: Rep[Query[(DomainA, DomainB)]]
    ): Rep[Query[Range]] =
        if (!clause.asDistinct) {
            projection (relation, clause.projection)
        }
        else
        {
            duplicateElimination (projection (relation, clause.projection))
        }   */


    def transform[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Range: Manifest] (
        clause: SelectClause3[DomainA, DomainB, DomainC, Range],
        relation: Rep[Query[(DomainA, DomainB, DomainC)]]
    ): Rep[Query[Range]] =
        if (!clause.asDistinct) {
            projection (relation, clause.projection)
        }
        else
        {
            duplicateElimination (projection (relation, clause.projection))
        }


    def transform[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        clause: SelectClause4[DomainA, DomainB, DomainC, DomainD, Range],
        relation: Rep[Query[(DomainA, DomainB, DomainC, DomainD)]]
    ): Rep[Query[Range]] =
        if (!clause.asDistinct) {
            projection (relation, clause.projection)
        }
        else
        {
            duplicateElimination (projection (relation, clause.projection))
        }


    def transform[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        clause: SelectClause5[DomainA, DomainB, DomainC, DomainD, DomainE, Range],
        relation: Rep[Query[(DomainA, DomainB, DomainC, DomainD, DomainE)]]
    ): Rep[Query[Range]] =
        if (!clause.asDistinct) {
            projection (relation, clause.projection)
        }
        else
        {
            duplicateElimination (projection (relation, clause.projection))
        }

}
