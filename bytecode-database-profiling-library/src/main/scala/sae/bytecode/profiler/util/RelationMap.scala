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
package sae.bytecode.profiler.util

import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.BytecodeDatabase
import sae.Relation

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 01.09.12
 * Time: 11:02
 *
 * A map that can store results for functions that return a relation.
 * Lookup is performed by computing the outcome of a given function on the database vs. the stored functions.
 */
class RelationMap[V]
{

    var values: Map[BytecodeDatabase => Relation[_ <: AnyRef], V] = Map.empty

    private def equalFunctionResult[T <: AnyRef](f1: BytecodeDatabase => Relation[T], f2: BytecodeDatabase => Relation[_ <: AnyRef]): Boolean = {
        val db = BATDatabaseFactory.create ()
        val v1 = f1 (db)
        val v2 = f2 (db)
        //f1 (db) == f2 (db)
        v1 == v2
    }

    def apply[T <: AnyRef](key: BytecodeDatabase => Relation[T]): V = {
        for (oldKey <- values.keys) {
            if (equalFunctionResult (key, oldKey)) {
                return values (oldKey)
            }
        }
        throw new IllegalArgumentException ("key not found " + key)
    }

    def update[T <: AnyRef](key: BytecodeDatabase => Relation[T], value: V): RelationMap[V] = {
        for (oldKey <- values.keys) {
            if (equalFunctionResult (key, oldKey)) {
                values = values.updated (key, value)
                return this
            }
        }
        values += (key -> value)
        this
    }
}
