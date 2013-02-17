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
package sae.operators.impl

import sae.operators.RecursiveView
import sae.deltas.{Update, Deletion, Addition}
import sae.Relation

/**
 *
 * @author Ralf Mitschke
 *
 */

case class RecursiveBase[Domain](relation: Relation[Domain])
    extends RecursiveView[Domain]
{
    relation.addObserver (this)

    var nextElement: Option[Domain] = None

    var recursiveNotification = false

    def added(v: Domain) {
        if (recursiveNotification) {
            nextElement = Some (v)
            return
        }

        recursiveNotification = true
        element_added (v)

        while (nextElement.isDefined) {
            val next = nextElement.get
            nextElement = None
            element_added (next)
        }
        recursiveNotification = false
    }

    def removed(v: Domain) {
        if (recursiveNotification) {
            nextElement = Some (v)
            return
        }

        recursiveNotification = true
        element_removed (v)

        while (nextElement.isDefined) {
            val next = nextElement.get
            nextElement = None
            element_removed (next)
        }
        recursiveNotification = false
    }

    def updated(oldV: Domain, newV: Domain) {
        throw new UnsupportedOperationException
    }

    def updated[U <: Domain](update: Update[U]) {
        throw new UnsupportedOperationException
    }

    def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        throw new UnsupportedOperationException
    }

}
