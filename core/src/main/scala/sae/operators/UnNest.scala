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
package sae.operators

import sae.{SelfMaintainedView, Relation}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 11.09.12
 * Time: 17:23
 */

trait UnNest[Range <: AnyRef, Domain <: AnyRef]
    extends Relation[Range]
{
    type Rng = Range

    def relation: Relation[Domain]

    def unNestFunction: Domain => Traversable[Range]
}

class UnNesting[Range <: AnyRef, Domain <: AnyRef](
                                                      val relation: Relation[Domain],
                                                      val unNestFunction: Domain => Seq[Range]
                                                      )
    extends UnNest[Range, Domain]
    with SelfMaintainedView[Domain, Range]
{
    /**
     * Applies f to all elements of the view.
     * Implementers must guarantee that no update/add/remove event is
     * fired during the deferred iteration
     */
    def lazy_foreach[T](f: (Range) => T) {
        relation.lazy_foreach (
            unNestFunction (_).foreach (f)
        )
    }

    /**
     * Each materialized view must be able to
     * materialize it's content from the underlying
     * views.
     * The laziness allows a query to be set up
     * on relations (tables) that are already filled.
     * thus the first call to foreach will try to
     * materialize already persisted data.
     */
    def lazyInitialize() {
        initialized = true
    }

    // TODO we could try and see whether the returned unestings are equal, but it is not
    def updated_internal(oldV: Domain, newV: Domain) {
        removed_internal (oldV)
        added_internal (newV)
    }

    def removed_internal(v: Domain) {
        unNestFunction (v).foreach (element_removed)
    }

    def added_internal(v: Domain) {
        unNestFunction (v).foreach (element_added)
    }
}


trait UnNestProject[Range <: AnyRef, UnnestRange <: AnyRef, Domain <: Range]
    extends Relation[Range]
{
    type Rng = Range

    def relation: Relation[Domain]

    def unNestFunction: Domain => Seq[UnnestRange]

    def projection: (Domain, UnnestRange) => Range
}

class UnNestingWithProjection[Range <: AnyRef, UnnestRange <: AnyRef, Domain <: Range](val relation: Relation[Domain],
                                                                                       val unNestFunction: Domain => Seq[UnnestRange],
                                                                                       val projection: (Domain, UnnestRange) => Range
                                                                                          )
    extends UnNestProject[Range, UnnestRange, Domain]
    with SelfMaintainedView[Domain, Range]
{
    /**
     * Applies f to all elements of the view.
     * Implementers must guarantee that no update/add/remove event is
     * fired during the deferred iteration
     */
    def lazy_foreach[T](f: (Range) => T) {
        relation.lazy_foreach ((v: Domain) =>
            unNestFunction (v).foreach ((u: UnnestRange) =>
                f (projection (v, u))
            )
        )
    }

    /**
     * Each materialized view must be able to
     * materialize it's content from the underlying
     * views.
     * The laziness allows a query to be set up
     * on relations (tables) that are already filled.
     * thus the first call to foreach will try to
     * materialize already persisted data.
     */
    def lazyInitialize() {
        initialized = true
    }

    // TODO we could try and see whether the returned unestings are equal, but it is not
    def updated_internal(oldV: Domain, newV: Domain) {
        removed_internal (oldV)
        added_internal (newV)
    }

    def removed_internal(v: Domain) {
        unNestFunction (v).foreach ((u: UnnestRange) =>
            element_removed (projection (v, u))
        )
    }

    def added_internal(v: Domain) {
        unNestFunction (v).foreach ((u: UnnestRange) =>
            element_added (projection (v, u))
        )
    }
}
