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
package sae.analyses.findbugs.random.oo

import sae.Relation
import sae.syntax.sql._
import sae.bytecode.structure._
import sae.bytecode.BytecodeDatabase
import sae.bytecode.instructions._
import java.util.regex.Pattern

/**
 *
 * @author Ralf Mitschke
 *
 */

object SIC_INNER_SHOULD_BE_STATIC_ANON
    extends (BytecodeDatabase => Relation[ClassDeclaration])
{
    val withinAnonymousClass = Pattern.compile ("[$][0-9].*[$]")

    /**
     * A heuristic for determining whether an inner class is inside an anonymous inner class based on the class name
     */
    def isWithinAnonymousInnerClass: ClassDeclaration => Boolean = {
        c => withinAnonymousClass.matcher (c.classType.className).find ()
    }

    def lastIndexOfInnerClassEncoding(classFile: ClassDeclaration): Int = {
        val name = classFile.classType.className
        math.max (name.lastIndexOf ('$'), name.lastIndexOf ('+'))
    }

    /**
     * A heuristic for determining inner classes by the encoding in the name
     */
    def isInnerClass: ClassDeclaration => Boolean = {
        c => lastIndexOfInnerClassEncoding (c) >= 0
    }

    /**
     * A heuristic for determining anonymous inner classes by the encoding in the name
     */
    def isAnonymousInnerClass: ClassDeclaration => Boolean = (c => {
        val lastSpecialChar = lastIndexOfInnerClassEncoding (c)
        isInnerClass (c) &&
            Character.isDigit (c.classType.className.charAt (lastSpecialChar + 1))
    })


    /**
     * A heuristic for determining whether an inner class can be made static
     */
    def canConvertToStaticInnerClass: ClassDeclaration => Boolean = {
        c => !isWithinAnonymousInnerClass (c)
    }

    /**
     * A heuristic for determining whether the field points to the enclosing instance
     */
    def isOuterThisField: FieldDeclaration => Boolean = {
        field => field.name.startsWith ("this$") || field.name.startsWith ("this+")
    }


    def apply(database: BytecodeDatabase): Relation[ClassDeclaration] = {
        import database._
        lazy val anonymousConvertable = compile (
            SELECT (*) FROM classDeclarations WHERE
                isAnonymousInnerClass AND
                canConvertToStaticInnerClass
        )

        lazy val unreadOuterThisField =
            compile (
                SELECT (*) FROM fieldDeclarations WHERE
                    isOuterThisField AND
                    NOT (
                        EXISTS (
                            SELECT (*) FROM readField WHERE
                                (((_: FieldReadInstruction).receiverType) === ((_: FieldDeclaration).declaringType)) AND
                                (((_: FieldReadInstruction).name) === ((_: FieldDeclaration).name)) AND
                                (((_: FieldReadInstruction).fieldType) === ((_: FieldDeclaration).fieldType))
                        )
                    )
            )

        lazy val classWithUnreadOuterField: Relation[ClassDeclaration] =
            SELECT ((c: ClassDeclaration, f: FieldDeclaration) => c) FROM
                (anonymousConvertable, unreadOuterThisField) WHERE
                (classType === declaringType)


        lazy val aload_1 =
            compile (
                SELECT (*) FROM instructions WHERE (_.isInstanceOf[ALOAD_1])
            )

        /**
         * A heuristic that determines whether the outer this field is read, by counting aload_1 instructions
         * The count must be greater than 1, because the variable will be read once for storing it
         * into the field reference for the outer this instance.
         */
        lazy val constructorReadFirstParam: Relation[MethodDeclaration] =
            SELECT (*) FROM constructors WHERE NOT (
                EXISTS (
                    SELECT (*) FROM aload_1 WHERE (declaringClassType === declaringType)
                )
            )

        SELECT (*) FROM (classWithUnreadOuterField) WHERE
            NOT (
                EXISTS (
                    SELECT (*) FROM constructorReadFirstParam WHERE (declaringType === classType)
                )
            )
    }

}
