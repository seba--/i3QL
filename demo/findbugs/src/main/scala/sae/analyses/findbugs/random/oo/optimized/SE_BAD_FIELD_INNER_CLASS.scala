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
package sae.analyses.findbugs.random.oo.optimized

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.analyses.findbugs.base.oo.Definitions
import de.tud.cs.st.bat.ACC_STATIC
import de.tud.cs.st.bat.resolved.ObjectType
import structure.internal.UnresolvedInnerClassEntry

import sae.operators.impl.{EquiJoinView, TransactionalEquiJoinView}

/**
 *
 * @author Ralf Mitschke
 *
 */

object SE_BAD_FIELD_INNER_CLASS
    extends (BytecodeDatabase => Relation[(ObjectType, ObjectType)])
{

    def hasStaticFlag: UnresolvedInnerClassEntry => Boolean = e =>
        ACC_STATIC.element_of (e.accessFlags)

    def innerClass: ((ObjectType, ObjectType)) => ObjectType = _._1

    def outerClass: ((ObjectType, ObjectType)) => ObjectType = _._2

    def apply(database: BytecodeDatabase): Relation[(ObjectType, ObjectType)] = {
        import database._
        val definitions = Definitions (database)
        import definitions._

        lazy val nonStaticInner = compile (
            SELECT ((u: UnresolvedInnerClassEntry) => (u.innerClassType, u.outerClassType.get)) FROM unresolvedInnerClasses WHERE
                ((u: UnresolvedInnerClassEntry) => u.declaringType == u.innerClassType) AND
                (_.outerClassType.isDefined) AND
                NOT (hasStaticFlag)
        )

        lazy val directlySerializable = compile (
            SELECT (subType) FROM interfaceInheritance WHERE (_.superType == serializable)
        )

        lazy val serializableNonStaticInner =
            if (Definitions.transactional)
                new TransactionalEquiJoinView (
                    nonStaticInner,
                    directlySerializable,
                    innerClass,
                    thisClass,
                    (e: (ObjectType, ObjectType), s: ObjectType) => e
                )
            else
                new EquiJoinView (
                    nonStaticInner,
                    directlySerializable,
                    innerClass,
                    thisClass,
                    (e: (ObjectType, ObjectType), s: ObjectType) => e
                )

        SELECT (*) FROM serializableNonStaticInner WHERE NOT (
            EXISTS (
                SELECT (*) FROM subTypesOfSerializable WHERE
                    (thisClass === outerClass)
            )
        )

    }


}
