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
 * A cross product constructs all combinations of tuples in multiple relations.
 * Thus the cross product dramatically enlarges
 * the amount of tuples in it's output.
 * The new relations are anonymous tuples of the
 * warranted size and types of the cross product.
 *
 * IMPORTANT: The cross product is not a self-maintained view.
 * In order to compute the delta of adding a tuple
 * to one of the underlying relations,
 * the whole other relation needs to be considered.
 *
 * This cross product  has the most general form, where a projection is immediately applied
 * without generating a tuple object for results.
 * The form where a tuple (DomainA,DomainB) is returned is more specific and can always be emulated by providing a
 * respective function.
 *
 * @author Ralf Mitschke
 *
 */
trait CrossProduct[DomainA, DomainB, Range]
    extends View[Range]
{

    def left: Relation[DomainA]

    def right: Relation[DomainB]

    def projection: (DomainA, DomainB) => Range

    override def children() = List (left, right)

    override def prettyprint(implicit prefix: String) = prefix +
      s"CrossProduct($projection, ${nested(left)}, ${nested(right)})"

}
