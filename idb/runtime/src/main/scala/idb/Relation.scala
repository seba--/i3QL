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
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
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
package idb


import java.io.PrintStream

import idb.observer.Observable
import idb.collections.impl.{MaterializedBag, MaterializedSet}


/**
 *
 *
 * A relation is the base trait for all operators and views.
 *
 * All relations can be iterated over.
 * The iteration requires that this relation or view is materialized, or one of the underlying relation is materialized.
 * If no relation whatsoever is materialized the iteration returns no elements.
 *
 * All relations can be materialized, even if they do not store elements themselves.
 *
 *
 * @author Ralf Mitschke
 */
trait Relation[+V]
    extends Observable[V]
    with Serializable
{
    /**
     * Runtime information whether a compiled query is a set or a bag
     */
    def isSet: Boolean

    def foreach[T] (f: (V) => T)


    def children: Seq[Relation[_]]

    /**
     * Converts the data of the view into a list representation.
     * This can be a costly operation and should mainly be used for testing.
     */
    def asList: List[V] = {
        var l: List[V] = List ()
        foreach (v =>
        {
            l = l :+ v
        }
        )
        l
    }

    /**
     * Always return the same materialized view for this relation
     */
    def asMaterialized: MaterializedView[V] = materializedRelation


    protected lazy val materializedRelation: MaterializedView[V] = {
        if (isSet) {
            new MaterializedSet[V](this)
        }
        else
        {
            new MaterializedBag[V](this)
        }
    }

    private def resetChildren(): Unit = {
        children.foreach(_.reset())
    }

    def reset(): Unit = {
        resetChildren()
        resetInternal()
    }

    protected def resetInternal()


    def print(out : PrintStream = System.out): Unit = {
        printInternal(out)
    }

    protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit
    protected[idb] def printNested(out : PrintStream, rel: Relation[_])(implicit prefix: String): Unit = {
	    rel.printInternal(out)(prefix + "  ")
    }

}

