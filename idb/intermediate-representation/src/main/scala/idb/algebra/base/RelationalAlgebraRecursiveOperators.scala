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
package idb.algebra.base

import idb.query.QueryEnvironment

/**
 *
 * @author Ralf Mitschke
 *
 */

trait RelationalAlgebraRecursiveOperators
    extends RelationalAlgebraBase
{

	def transitiveClosure[Edge: Manifest, Vertex: Manifest] (
		relation: Rep[Query[Edge]],
		tail: Rep[Edge => Vertex],
		head: Rep[Edge => Vertex]
	)(implicit env : QueryEnvironment): Rep[Query[(Vertex,Vertex)]]

	def recursion[Domain : Manifest] (
	    base : Rep[Query[Domain]],
		result : Rep[Query[Domain]]
	)(implicit env : QueryEnvironment): Rep[Query[Domain]]

    /**
     * A recursion node is an ast node that denotes that elements from result will enter the computation recursively.
     * New results are added until a fix point is reached.
     */
    def recursionNode[Domain : Manifest] (
        base : Rep[Query[Domain]],
        result : Rep[Query[Domain]]
	)(implicit env : QueryEnvironment): Rep[Query[Domain]]

    /**
     * A recursion result is a special ast node.
     * It denotes that the query will enter a recursion and thus we are not allowed to make general optimizations.
     * For example, fusing projections would be illegal, since the type of the fused projection is not anymore
     * the type required for entering the recursion again.
     */
    def recursionResult[Domain: Manifest] (
        query: Rep[Query[Domain]],
        source: Rep[Query[Domain]]
    )(implicit env : QueryEnvironment): Rep[Query[Domain]]
}
