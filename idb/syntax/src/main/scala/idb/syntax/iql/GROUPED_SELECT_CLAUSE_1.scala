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
package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * A SELECT clause is converted implicitly to a grouped SELECT clause if the projection function takes arguments not
 * conforming to the supplied relations. In this case the relations can still be grouped,
 * such that the whole expression is well typed.
 * Note that the receiver is implicitly converted, e.g., the expression "SELECT ((s:Rep[String]) => s)".
 * This means that the parameters to FROM must be explicitly typed as extents, queries and compiled queries,
 * since the scala compiler can only apply one implicit conversion to make the whole expression well typed.
 *
 * @author Ralf Mitschke
 */
trait GROUPED_SELECT_CLAUSE_1[GroupKey, Range]
{
    /*
    def FROM[Domain: Manifest] (
        relation: Rep[Query[Domain]]
    ): GROUPED_FROM_CLAUSE_1[Domain, GroupKey, Range]

    def FROM[Domain: Manifest] (
        extent: Extent[Domain]
    ): GROUPED_FROM_CLAUSE_1[Domain, GroupKey, Range]
*/
}