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

import idb.algebra.base.RelationalAlgebraBasicOperators


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRBasicOperators
    extends RelationalAlgebraIRBase with RelationalAlgebraBasicOperators
{

    case class Projection[Domain, Range] (
        var relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    ) (implicit val mDom : Manifest[Domain], val mRan : Manifest[Range]) extends Def[Query[Range]] {

	}

    case class Selection[Domain: Manifest] (
        var relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ) extends Def[Query[Domain]]

    case class CrossProduct[DomainA: Manifest, DomainB: Manifest] (
        var relationA: Rep[Query[DomainA]],
        var relationB: Rep[Query[DomainB]]
    ) extends Def[Query[(DomainA, DomainB)]] {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB = implicitly[Manifest[DomainB]]
	}

    case class EquiJoin[DomainA: Manifest, DomainB: Manifest] (
        var relationA: Rep[Query[DomainA]],
		var relationB: Rep[Query[DomainB]],
        equalities: Seq[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ) extends Def[Query[(DomainA, DomainB)]]

	case class UnionAdd[DomainA <: Range : Manifest, DomainB <: Range :Manifest, Range : Manifest] (
		var relationA: Rep[Query[DomainA]],
		var relationB: Rep[Query[DomainB]]
	) extends Def[Query[Range]] {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB = implicitly[Manifest[DomainB]]
		val mRan = implicitly[Manifest[Range]]
	}

	case class UnionMax[DomainA <: Range : Manifest, DomainB <: Range :Manifest, Range : Manifest] (
		var relationA: Rep[Query[DomainA]],
		var relationB: Rep[Query[DomainB]]
	) extends Def[Query[Range]] {
		val mDomA = implicitly[Manifest[DomainA]]
		val mDomB = implicitly[Manifest[DomainB]]
		val mRan = implicitly[Manifest[Range]]
	}

	case class Intersection[Domain : Manifest] (
		var relationA: Rep[Query[Domain]],
		var relationB: Rep[Query[Domain]]
	) extends Def[Query[Domain]]

	case class Difference[Domain : Manifest] (
		var relationA: Rep[Query[Domain]],
		var relationB: Rep[Query[Domain]]
	) extends Def[Query[Domain]]

	case class SymmetricDifference[Domain : Manifest] (
		var relationA: Rep[Query[Domain]],
		var relationB: Rep[Query[Domain]]
	) extends Def[Query[Domain]]

	case class DuplicateElimination[Domain : Manifest] (
		var relation: Rep[Query[Domain]]
	) extends Def[Query[Domain]]

	case class TransitiveClosure[Edge: Manifest, Vertex: Manifest] (
		var relation: Rep[Query[Edge]],
		tail: Rep[Edge => Vertex],
		head: Rep[Edge => Vertex]
	) extends Def[Query[(Vertex,Vertex)]]

	case class Unnest[Domain: Manifest, Range: Manifest] (
		var relation: Rep[Query[Domain]],
		unnesting: Rep[Domain => Seq[Range]]
	) extends Def[Query[Range]]

	class Recursion[Domain : Manifest] (
		var base: Rep[Query[Domain]],
		var result: Rep[Query[Domain]]
	) extends Def[Query[Domain]] {

	}

    def projection[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    ): Rep[Query[Range]] =
        Projection (relation, function)

    def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] =
        Selection (relation, function)

    def crossProduct[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): Rep[Query[(DomainA, DomainB)]] =
        CrossProduct (relationA, relationB)

    def equiJoin[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        equalities: Seq[(Rep[DomainA => Any], Rep[DomainB => Any])]
    ): Rep[Query[(DomainA, DomainB)]] =
        EquiJoin (relationA, relationB, equalities)

	def unionAdd[DomainA <: Range : Manifest, DomainB <: Range :Manifest, Range : Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]]
		): Rep[Query[Range]] =
		UnionAdd (relationA, relationB)

	def unionMax[DomainA <: Range : Manifest, DomainB <: Range :Manifest, Range : Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]]
	): Rep[Query[Range]] =
		UnionMax (relationA, relationB)

	def intersection[Domain : Manifest] (
		relationA: Rep[Query[Domain]],
		relationB: Rep[Query[Domain]]
		): Rep[Query[Domain]] =
		Intersection(relationA, relationB)

	def difference[Domain : Manifest] (
		relationA: Rep[Query[Domain]],
		relationB: Rep[Query[Domain]]
	): Rep[Query[Domain]] =
		Difference (relationA, relationB)

	def symmetricDifference[Domain : Manifest] (
		relationA: Rep[Query[Domain]],
		relationB: Rep[Query[Domain]]
	): Rep[Query[Domain]] =
		SymmetricDifference (relationA, relationB)

	def duplicateElimination[Domain : Manifest] (
		relation: Rep[Query[Domain]]
	): Rep[Query[Domain]] =
		DuplicateElimination (relation)

	def transitiveClosure[Edge: Manifest, Vertex: Manifest] (
		relation: Rep[Query[Edge]],
		tail: Rep[Edge => Vertex],
		head: Rep[Edge => Vertex]
	): Rep[Query[(Vertex,Vertex)]] =
		TransitiveClosure (relation, tail, head)

	def unnest[Domain: Manifest, Range: Manifest] (
		relation: Rep[Query[Domain]],
		unnesting: Rep[Domain => Seq[Range]]
	): Rep[Query[Range]] =
		Unnest (relation, unnesting)

	def recursion[Domain : Manifest, Range : Manifest] (
		base : Rep[Query[Domain]],
		result : Rep[Query[Domain]]
	): Rep[Query[Domain]] = {
		findRecursionBase(result, base)
		changeRecursionResult(result,result)
		return result
	}

	/**
	 * Searches the recursion base in a operator tree and inserts a recursion node next-to-last to the recursion base.
	 * @param base The base of the recursion.
	 * @param relation The operator tree where the recursion should be added. The tree must contain the recursion base.
	 * @return An operator tree with a recursion node. Note that the result of the recursion node is NOT set accordingly to the definition of the recursion.
	 */
	private def findRecursionBase[Domain : Manifest](relation : Rep[Query[Domain]], base : Rep[Query[_]]) {
		relation match {

			case QueryRelation (r, _, _) => throw new IllegalArgumentException("The base was not found in the result tree.")
			case QueryExtent (e, _, _) => throw new IllegalArgumentException("The base was not found in the result tree.")

			case Def (e@Projection (r, f)) => {
				setRecursionBase(r, base, x => e.relation = x)
			}
			case Def (e@Selection (r, f)) => {
				setRecursionBase(r,base, x => e.relation = x)
			}
			case Def (e@CrossProduct (a, b)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@EquiJoin (a, b, eq)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@UnionAdd (a, b)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@UnionMax (a, b)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@Intersection (a, b)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@Difference (a, b)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@SymmetricDifference (a, b)) => {
				setRecursionBase(a,b,base,x => e.relationA = x, x => e.relationB = x)
			}
			case Def (e@DuplicateElimination (a)) => {
				setRecursionBase(a,base, x => e.relation = x)
			}
			case Def (e@TransitiveClosure (a, t, h)) => {
				setRecursionBase(a,base, x => e.relation = x)
			}
			case Def (e@Unnest (a, f)) => {
				setRecursionBase(a,base,x => e.relation = x)
			}
/*			case Def (Recursion (b, r)) => {
				setRecursionBase(b,base, Recursion(b ,r))
			}  */
			case e => {
				throw new IllegalArgumentException("Could not traverse through " + e)
			}
		}
	}

	/**
	 * Helper function that inserts the recursion node into the operator tree, if the base has been reached. Otherwise the operator tree is further traversed.
	 * @param relation The operator tree to be checked.
	 * @param base The base that should be found.
	 * @param setFunction The function to be applied for traversal.
	 * @return An operator tree where the recursive node is set accordingly at the node relation.
	 */
	private def setRecursionBase[Domain : Manifest](
		relation : Rep[Query[Domain]],
		base : Rep[Query[_]],
		setFunction : Rep[Query[Domain]] => _
	) {
			if(relation == base){
				val rec = Recursion(base.asInstanceOf[Rep[Query[Domain]]],base.asInstanceOf[Rep[Query[Domain]]])
				setFunction(rec)
			} else {
				findRecursionBase(relation,base)
			}

	}

	private def setRecursionBase[DomainA : Manifest, DomainB : Manifest](
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		base : Rep[Query[_]],
		setFunctionA : Rep[Query[DomainA]] => _,
		setFunctionB : Rep[Query[DomainB]] => _
	) {
		try {
			setRecursionBase[DomainA,Range](relationA,base,setFunctionA)
		} catch {
			case e : IllegalArgumentException => setRecursionBase[DomainB,Range](relationB,base,setFunctionB)
		}
	}

	/**
	 * Sets the result of the first encountered recursion to the new result.
	 * @param relation The operator tree to be traversed.
	 * @param result The new result of the recursion.
	 */
	private def changeRecursionResult(relation : Rep[Query[_]], result : Rep[Query[_]]) : Boolean = {
		relation match {
			case Def (r@Recursion(base, oldResult)) => {
				r.setResult(result)
				true
			}
			case Def (Selection (r, f)) => {
				changeRecursionResult(r,result)
			}
			case Def (Projection (r, f)) => {
				changeRecursionResult(r,result)
			}
			case Def (CrossProduct (a, b)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (EquiJoin (a, b, eq)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (UnionAdd (a, b)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (UnionMax (a, b)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (Intersection (a, b)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (Difference (a, b)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (SymmetricDifference (a, b)) => {
				if(changeRecursionResult(a,result))
					true
				else
					changeRecursionResult(b,result)
			}
			case Def (DuplicateElimination (a)) => {
				changeRecursionResult(a,result)
			}
			case Def (TransitiveClosure (a, t, h)) => {
				changeRecursionResult(a,result)
			}
			case Def (Unnest (a, f)) => {
				changeRecursionResult(a,result)
			}

			case _ => false
		}
	}


}
