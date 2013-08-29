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

import idb.algebra.base.RelationalAlgebraRecursiveOperators

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRRecursiveOperators
    extends RelationalAlgebraIRBase
	with RelationalAlgebraRecursiveOperators
	with RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRSetTheoryOperators
	with RelationalAlgebraIRAggregationOperators
{

    case class TransitiveClosure[Edge: Manifest, Vertex: Manifest] (
        var relation: Rep[Query[Edge]],
        tail: Rep[Edge => Vertex],
        head: Rep[Edge => Vertex]
    ) extends Def[Query[(Vertex, Vertex)]] with QueryBaseOps {
        val mEdge = implicitly[Manifest[Edge]]
        val mVertex = implicitly[Manifest[Vertex]]

		def isMaterialized: Boolean = !isIncrementLocal //Transitive closure is materialized
		def isSet = false
		def isIncrementLocal = false
    }

   case class Recursion[Domain: Manifest] (
        var base: Rep[Query[Domain]],
        var result: Rep[Query[Domain]]
    ) extends Def[Query[Domain]] with QueryBaseOps {
		def isMaterialized: Boolean = result.isMaterialized
		def isSet = false
		def isIncrementLocal = false
    }



    def transitiveClosure[Edge: Manifest, Vertex: Manifest] (
        relation: Rep[Query[Edge]],
        tail: Rep[Edge => Vertex],
        head: Rep[Edge => Vertex]
    ): Rep[Query[(Vertex, Vertex)]] =
        TransitiveClosure (relation, tail, head)

   def recursion[Domain: Manifest] (
        base: Rep[Query[Domain]],
        result: Rep[Query[Domain]]
    ): Rep[Query[Domain]] = {
        insertRecursionAtBase (result, base, result, (x: Rep[Query[Domain]]) => {})
        return result
    }

    /**
     * Searches the recursion base in a operator tree and inserts a recursion node next-to-last to the recursion base.
     * @param base The base of the recursion.
     * @param relation The operator tree where the recursion should be added. The tree must contain the recursion base.
     * @return An operator tree with a recursion node. Note that the result of the recursion node is NOT set
     *         accordingly to the definition of the recursion.
     */
    private def insertRecursionAtBase[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        base: Rep[Query[_]],
        result: Rep[Query[_]],
        setFunction: (Rep[Query[Domain]]) => Unit
    ) {
		relation match
        {
            case `base` =>
            {
                setFunction (Recursion (relation, result.asInstanceOf[Rep[Query[Domain]]]))
            }
            case QueryRelation (r, _, _, _) => throw new IllegalArgumentException ("The base was not found in the " +
                "result tree.")
            case QueryExtent (e, _, _, _) => throw new IllegalArgumentException ("The base was not found in the " +
                "result tree.")

            // TODO why is sometimes the matched r used and sometimes e.relation?
            case Def (e@Projection (r, _)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Any]]) => e.relation = x)
            }
            case Def (e@Selection (r, _)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Domain]]) => e.relation = x)
            }
            case Def (e@CrossProduct (a, b)) =>
            {
                setRecursionBase (a, b, base, result, (x: Rep[Query[Any]]) => e.relationA = x,
                    (x: Rep[Query[Any]]) => e.relationB = x)
            }
            case Def (e@EquiJoin (a, b, _)) =>
            {
                setRecursionBase (a, b, base, result, (x: Rep[Query[Any]]) => e.relationA = x,
                    (x: Rep[Query[Any]]) => e.relationB = x)
            }
            case Def (e@UnionAdd (a, b)) =>
            {
                setRecursionBase (a, b, base, result, (x: Rep[Query[Domain]]) => e.relationA = x,
                    (x: Rep[Query[Domain]]) => e.relationB = x)
            }
            case Def (e@UnionMax (a, b)) =>
            {
                setRecursionBase (a, b, base, result, (x: Rep[Query[Domain]]) => e.relationA = x,
                    (x: Rep[Query[Domain]]) => e.relationB = x)
            }
            case Def (e@Intersection (a, b)) =>
            {
                setRecursionBase (a, b, base, result, (x: Rep[Query[Domain]]) => e.relationA = x,
                    (x: Rep[Query[Domain]]) => e.relationB = x)
            }
            case Def (e@Difference (a, b)) =>
            {
                setRecursionBase (a, b, base, result, (x: Rep[Query[Domain]]) => e.relationA = x,
                    (x: Rep[Query[Domain]]) => e.relationB = x)
            }
            case Def (e@DuplicateElimination (r)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Domain]]) => e.relation = x)
            }
            case Def (e@TransitiveClosure (r, t, h)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Any]]) => e.relation = x)
            }
            case Def (e@Unnest (r, f)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Any]]) => e.relation = x)
            }
            case Def (e@AggregationSelfMaintained (r, _, _, _, _, _, _)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Any]]) => e.relation = x)
            }
            case Def (e@Recursion (r, _)) =>
            {
                insertRecursionAtBase (r, base, result, (x: Rep[Query[Domain]]) => e.base = x)
            }
            case e =>
            {
                throw new IllegalArgumentException ("Could not traverse through " + e)
            }
        }
    }

    private def setRecursionBase[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        base: Rep[Query[_]],
        result: Rep[Query[_]],
        setFunctionA: Rep[Query[DomainA]] => Unit,
        setFunctionB: Rep[Query[DomainB]] => Unit
    ) {
        try
        {
            insertRecursionAtBase (relationA, base, result, setFunctionA)
        } catch
            {
                case e: IllegalArgumentException => insertRecursionAtBase (relationB, base, result, setFunctionB)
            }
    }




}

