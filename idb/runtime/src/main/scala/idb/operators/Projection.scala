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
package idb.operators

import idb.{View, Relation}


/**
 * A projection operates as a filter on the relation and eliminates unwanted
 * constituents from the tuples.
 * Thus the projection shrinks the size of relations.
 * The new relations are either a new kind of object or anonymous tuples of the
 * warranted size and types of the projection.
 * Important Note:
 * E.Codd in his seminal work: RELATIONAL COMPLETENESS OF DATA BASE SUBLANGUAGES
 * defined projection as a set operations.
 * Thus the newResult does NOT contain duplicates.
 * According to other papers this treatment of duplicates complicates things
 * (i.e., in the translation from relational calculus to relational algebra? - TODO check).
 * In particular the following property is not guaranteed:
 * R intersect S is subset of R.
 * In set theory this is trivial. However with the use of duplicates the following situation arises:
 * R := a | b  S := a | b
 * u | v       u | v
 *
 * Definition of intersection in relational calculus
 * R intersect S = ( R[1,2 = 1,2]S )[1,2].
 * Reads as: R joined with S where column 1 and column 2 are equal and
 * the newResult contains  column 1 and column 2.
 * Since the projection in the join:
 * R intersect S := a | b
 * u | v
 * a | b
 * u | v
 *
 * Specialized classes for SQL semantics are available (see further below).
 * In general the Projection is an operation that takes a projection function
 * from domain to range and a relation of range tuples.
 * The parameterized types are accessible as members for use in
 * constructors during pattern matching
 */
trait Projection[Domain, Range]
    extends View[Range]
{
    def projection: Domain => Range

    def relation: Relation[Domain]

    def children = List (relation)

    override def prettyprint(implicit prefix: String) = prefix +
      s"Projection($projection, ${nested(relation)})"

}
