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


/**
 *
 * The operators defined in the database are based on the concept of self-maintainable views.
 *
 * A view is self-maintainable, if the maintenance operations are expressible in terms of the tuples
 * changed during insertions, deletions and updates, as well as the data of the view itself.
 * No data of the full underlying relation may be used to maintain the view.
 * </br>
 * We consider the self-maintenance problem for arbitrary SPJ (Select, Project, Join) views, i.e.,
 * views as defined by SQL statements.
 * In addition views may be defined for recursive queries using special operators, that will
 * be further defined.
 * TODO reference operators allowed for recursion.
 * </br>
 * A self maintained view observes a relation of type V and provides a view of a relation of type Z.
 * In case of joins, or other entities that can have entries of multiple types for V, we use tuples.
 *
 * Theory and background:
 * Insertions:
 *
 * Theorem 1: An SPJ view, that takes the join of two or more distinct relations is not self-maintainable with
 * respect to insertions. Essentially the new tuple must be joined with all tuples of the other underlying relation.
 * Thus the whole other relation is required.
 *
 * Theorem 2: All SP views are self-maintainable with respect to insertions.
 *
 * Theorem 3: An SPJ view defined using self-joins over a single relation R is self-maintainable if every join is
 * based on key(R)
 *
 * @author Ralf Mitschke
 *
 */
package object idb
{


}
