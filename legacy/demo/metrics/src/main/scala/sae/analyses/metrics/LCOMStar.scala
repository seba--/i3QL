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
package sae.analyses.metrics

import sae.Relation
import de.tud.cs.st.bat.resolved.{ObjectType, ReferenceType}
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode._
import instructions.InstructionInfo
import sae.functions.Count
import structure.{FieldInfo, MethodInfo, FieldDeclaration, MethodDeclaration}
import sae.syntax.sql._


/**
 *
 * @author Ralf Mitschke
 *
 */

object LCOMStar
    extends (BytecodeDatabase => Relation[(ObjectType, Option[Double])])
{

    type FieldDependency = InstructionInfo with FieldInfo

    def isNeitherConstructOrStaticInitializer: MethodInfo => Boolean = (m: MethodInfo) => m.name != "<init>" && m.name != "<cinit>"

    def isNeitherInConstructOrStaticInitializer: InstructionInfo => Boolean = (i: InstructionInfo) => i.declaringMethod.name != "<init>" && i.declaringMethod.name != "<cinit>"

    //def readDependency: FieldReadInstruction => (MethodDeclaration, FieldInfo) = (i: FieldReadInstruction) => (i.declaringMethod, i.instruction)

    //def writeDependency: FieldWriteInstruction => (MethodDeclaration, FieldInfo) =>

    def countedType: ((ObjectType, Int)) => ObjectType = _._1

    def countedType2: ((ObjectType, Int, Int)) => ObjectType = _._1

    def mergeCounts: ((ObjectType, Int), (ObjectType, Int)) => (ObjectType, Int, Int) = (e1: (ObjectType, Int), e2: (ObjectType, Int)) => (e1._1, e1._2, e2._2)

    def getLCOMCounter: ((ObjectType, Int, Int), (ObjectType, Int)) => LCOMCounter = (e1: (ObjectType, Int, Int), e2: (ObjectType, Int)) => LCOMCounter (e1._1, e1._2, e1._3, e2._2)

    case class LCOMCounter(classType: ObjectType, numberOfMethods: Int, numberOfFields: Int, numberOfFieldAccesses: Int)
    {
        def lcom: (ObjectType, Option[Double]) = {
            if (this.numberOfFields == 0 || this.numberOfFields == 1 || this.numberOfMethods == 0) (this.classType, None)
            val a: Double = 1.asInstanceOf[Double] / this.numberOfFields.asInstanceOf[Double]
            val b: Double = a * this.numberOfFieldAccesses.asInstanceOf[Double] - this.numberOfMethods.asInstanceOf[Double]
            val c: Double = 1 - this.numberOfMethods
            if (c == 0)
                return (this.classType, None)
            val d: Double = b / c
            if (d == 0 || d == -0)
                return (this.classType, Some (0))
            if (d < 0)
                return (this.classType, None)
            (this.classType, Some (d))
        }
    }

    /**
     * Calculates the lcom* metric for all classes in db
     * These modified lcom* metric excludes constructors and static initializer from the calculation
     */
    def apply(database: BytecodeDatabase): Relation[(ObjectType, Option[Double])] = {
        import database._
        val methods = compile (
            SELECT (*) FROM methodDeclarations WHERE isNeitherConstructOrStaticInitializer
        )

        val methodUseField = compile (
            SELECT (*) FROM readField.asInstanceOf[Relation[FieldDependency]] WHERE
                isNeitherInConstructOrStaticInitializer AND
                ((i: FieldDependency) => i.declaringMethod.declaringClassType == i.declaringType) UNION_ALL (
                SELECT (*) FROM writeField.asInstanceOf[Relation[FieldDependency]] WHERE
                    isNeitherInConstructOrStaticInitializer AND
                    ((i: FieldDependency) => i.declaringMethod.declaringClassType == i.declaringType)
                )
        )

        //number of fields per class
        val numberOfFields: Relation[(ObjectType, Int)] =
            γ (fieldDeclarations, declaringType, Count[FieldDeclaration]())


        //number of methods per class
        val numberOfMethods: Relation[(ObjectType, Int)] =
            γ (methods, declaringType, Count[MethodDeclaration]())


        // Sum_allMethods( distinct class field access)
        val countRWtoOneField: Relation[(ObjectType, Int)] =
            γ (methodUseField, declaringClassType, Count[FieldDependency]())



        val elementsPerClass = compile (
            SELECT (mergeCounts) FROM (numberOfFields, numberOfMethods) WHERE
                (countedType === countedType)
        )

        val lcomCounters = compile (
            SELECT (getLCOMCounter) FROM (elementsPerClass, countRWtoOneField) WHERE
                (countedType2 === countedType)
        )

        SELECT ((_: LCOMCounter).lcom) FROM lcomCounters

    }

}
