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

import idb.query.QueryEnvironment

import scala.language.implicitConversions
import sae.bytecode.BytecodeDatabase
import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 *
 * @author Ralf Mitschke
 */
trait ClassDependencies
{
	private implicit val queryEnvironment = QueryEnvironment.Default

    def classDependencies (database: BytecodeDatabase): Rep[Query[Dependency[database.ObjectType]]] = {
        import database._
        // field types
        (SELECT ((f: Rep[FieldDeclaration]) =>
            Dependency (f.declaringType, f.fieldType.AsInstanceOf[ObjectType])
        ) FROM database.fieldDeclarations WHERE (_.fieldType.IsInstanceOf[ObjectType])

            ) UNION ALL (
            // return types
            SELECT ((m: Rep[MethodDeclaration]) =>
                Dependency (m.declaringType, m.returnType.AsInstanceOf[ObjectType])
            ) FROM methodDeclarations WHERE (_.returnType.IsInstanceOf[ObjectType])

        ) UNION ALL (
            // parameter types
            SELECT ((p: Rep[(MethodDeclaration, FieldType)]) =>
                Dependency (p._1.declaringType, p._2.AsInstanceOf[ObjectType])
            ) FROM UNNEST (methodDeclarations, (_: Rep[MethodDeclaration]).parameterTypes) WHERE (
                (p: Rep[(MethodDeclaration, FieldType)]) => p._2.IsInstanceOf[ObjectType]
                )

        ) UNION ALL (
            // super class and super interface types
            SELECT ((i: Rep[Inheritance]) =>
                Dependency (i.declaringClass.classType, i.superType)
            ) FROM inheritance

        ) UNION ALL (
            // called method receiver types
            SELECT ((i: Rep[MethodInvocationInstruction]) =>
                Dependency (i.declaringMethod.declaringType, i.methodInfo.receiverType.AsInstanceOf[ObjectType])
            ) FROM methodInvocationInstructions WHERE (_.methodInfo.receiverType.IsInstanceOf[ObjectType])

        ) UNION ALL (
            // accessed field declaring types
            SELECT ((i: Rep[FieldAccessInstruction]) =>
                Dependency (i.declaringMethod.declaringType, i.fieldInfo.declaringType.AsInstanceOf[ObjectType])
            ) FROM fieldAccessInstructions WHERE (_.fieldInfo.declaringType.IsInstanceOf[ObjectType])

        ) UNION ALL (
            // new, check cast, etc.
            SELECT ((i: Rep[ObjectTypeInstruction]) =>
                Dependency (i.declaringMethod.declaringType, i.objectType)
            ) FROM objectTypeInstructions

        )
        /*
        // TODO new array instructions, does not compile?
        UNION ALL (
            // new array
            SELECT ((i: Rep[NewArrayInstruction[Any]]) =>
                Dependency (i.declaringMethod.declaringType, i.elementType.AsInstanceOf[ObjectType])
            ) FROM newArrayInstructions WHERE (_.elementType.IsInstanceOf[ObjectType])

        )
        */

        // TODO exception dependencies, (all or checked only?)
    }


    type Dependency[+T] = (T, T)

    implicit def dependencyToInfixOps[T: Manifest] (d: Rep[Dependency[T]]) =
        DependencyOps(d)

    case class DependencyOps[T: Manifest] (d: Rep[Dependency[T]])
    {
        def source: Rep[T] = d._1

        def target: Rep[T] = d._2

        //def isCrossPackage: Rep[Boolean] = field[Boolean](d, "isCrossPackage")
    }

    def Dependency[T:Manifest] (source: Rep[T], target: Rep[T]): Rep[Dependency[T]] =
        (source, target)

}
