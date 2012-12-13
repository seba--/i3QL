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
import sae.bytecode.structure._
import sae.bytecode._
import sae.bytecode.instructions._
import java.util.regex.Pattern
import sae.operators.impl.NotExistsInSameDomainView
import de.tud.cs.st.bat.resolved.{FieldType, ObjectType}
import sae.functions.Count
import sae.syntax.RelationalAlgebraSyntax.γ
import sae.analyses.findbugs.AnalysesOO

/**
 *
 * @author Ralf Mitschke
 *
 */

object SIC_INNER_SHOULD_BE_STATIC_ANON
        extends (BytecodeDatabase => Relation[ObjectType])
{


    val withinAnonymousClass = Pattern.compile("[$][0-9].*[$]")

    /**
     * A heuristic for determining whether an inner class is inside an anonymous inner class based on the class name
     */
    def isWithinAnonymousInnerClass: ClassDeclaration => Boolean = {
        c => withinAnonymousClass.matcher(c.classType.className).find()
    }

    def lastIndexOfInnerClassEncoding(classFile: ClassDeclaration): Int = {
        val name = classFile.classType.className
        math.max(name.lastIndexOf('$'), name.lastIndexOf('+'))
    }

    /**
     * A heuristic for determining inner classes by the encoding in the name
     */
    def isInnerClass: ClassDeclaration => Boolean = {
        c => lastIndexOfInnerClassEncoding(c) >= 0
    }

    /**
     * A heuristic for determining anonymous inner classes by the encoding in the name
     */
    def isAnonymousInnerClass: ClassDeclaration => Boolean = (c => {
        val lastSpecialChar = lastIndexOfInnerClassEncoding(c)
        isInnerClass(c) &&
                Character.isDigit(c.classType.className.charAt(lastSpecialChar + 1))
    })


    /**
     * A heuristic for determining whether an inner class can be made static
     */
    def canConvertToStaticInnerClass: ClassDeclaration => Boolean = {
        c => !isWithinAnonymousInnerClass(c)
    }

    /**
     * A heuristic for determining whether the field points to the enclosing instance
     */
    def isOuterThisField: FieldDeclaration => Boolean = {
        field => field.name.startsWith("this$") || field.name.startsWith("this+")
    }


    def apply(database: BytecodeDatabase): Relation[ObjectType] = {
        import database._


        lazy val outerThisField =
            compile(
                SELECT((f: FieldDeclaration) => (f.declaringType, f.name, f.fieldType)) FROM fieldDeclarations WHERE
                        isOuterThisField AND
                        (f => isAnonymousInnerClass(f.declaringClass)) AND
                        (f => canConvertToStaticInnerClass(f.declaringClass))
            )

        lazy val readFields =
            compile(
                SELECT((f: FieldReadInstruction) => (f.receiverType, f.name, f.fieldType)) FROM readField WHERE (
                        field => field.name.startsWith("this$") || field.name.startsWith("this+"))
            )

        lazy val notExists =
            if (AnalysesOO.existsOptimization)
                new NotExistsInSameDomainView(outerThisField.asMaterialized, readFields.asMaterialized)
            else
                compile(
                    SELECT(*) FROM outerThisField WHERE NOT(EXISTS(
                        SELECT(*) FROM readFields WHERE
                                ((identity[(ObjectType, String, FieldType)] _) === (identity[(ObjectType, String, FieldType)] _))
                    )
                    )
                )
        lazy val unreadOuterThisField =
            compile(
                SELECT((_: (ObjectType, String, FieldType))._1) FROM (notExists)
            )


        lazy val aload_1 =
            compile(
                SELECT(*) FROM instructions WHERE (_.isInstanceOf[ALOAD_1])
            )

        val aload_1InInnerClassConstructors = compile(
            SELECT(*) FROM aload_1 WHERE
                    (_.declaringMethod.name == "<init>") AND
                    (i => i.declaringMethod.declaringClassType.className.indexOf('$') > 0)
        )

        val countAload_1InInnerClassConstructors: Relation[(MethodDeclaration, Int)] = γ(
            aload_1InInnerClassConstructors,
            declaringMethod,
            Count[InstructionInfo](),
            (m: MethodDeclaration, count: Int) => (m, count)
        )


        val innerClassConstructorWithOneAload: Relation[ObjectType] =
            compile(
                SELECT((_: (MethodDeclaration, Int))._1
                        .declaringClassType) FROM countAload_1InInnerClassConstructors WHERE (_._2 > 1)
            )

        new NotExistsInSameDomainView(
            unreadOuterThisField.asMaterialized,
            innerClassConstructorWithOneAload.asMaterialized
        )
    }

}
