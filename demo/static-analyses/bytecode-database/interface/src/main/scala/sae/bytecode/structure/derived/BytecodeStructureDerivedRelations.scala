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
package sae.bytecode.structure.derived

import idb.query.QueryEnvironment

import scala.language.implicitConversions
import idb.syntax.iql._
import sae.bytecode.structure.base._

/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeStructureDerivedRelations
    extends BytecodeStructureDerived
    with BytecodeStructureDerivedConstructors
    with BytecodeStructureDerivedOps
    with BytecodeStructureRelations
    with BytecodeStructureOps
{
    override val IR = idb.syntax.iql.IR

    import IR._
	private implicit val queryEnvironment = QueryEnvironment.Default

    lazy val classInheritance: Rep[Query[Inheritance]] =
        SELECT (
            (c: Rep[ClassDeclaration]) => Inheritance (c, c.superType.get)
        ) FROM classDeclarations WHERE (_.superType.isDefined)


    // TODO why is the type annotation needed in UNNEST(classDeclarations,_.interfaces)
    lazy val interfaceInheritance: Rep[Query[Inheritance]] =
        SELECT (
            (pair: Rep[(ClassDeclaration, ObjectType)]) => Inheritance (pair._1, pair._2)
        ) FROM UNNEST (classDeclarations, (_: Rep[ClassDeclaration]).interfaces)
    /*
        // TODO this should also work
        SELECT (
            (c: Rep[ClassDeclaration], t:Rep[ ObjectType)]) => Inheritance (c, t)
        ) FROM UNNEST (classDeclarations, _.interfaces)
      */

    lazy val inheritance: Rep[Query[Inheritance]] =
        classInheritance UNION ALL (interfaceInheritance)


    // TODO get rid of casts. This is hard because pattern matching does not work well if T is is covariant
    // in Query[T]. It works for compiled relation, but actually in the interface we do not need to compile
    // all relations
    lazy val subTyping: Rep[Query[TypeRelation]] =
        SELECT DISTINCT * FROM (
            WITH RECURSIVE (
                (rec: Rep[Query[TypeRelation]]) =>
                    SELECT ((_: Rep[Inheritance]).asInstanceOf[Rep[TypeRelation]]) FROM inheritance
                        UNION ALL (
                        SELECT (
                            (t1: Rep[TypeRelation], t2: Rep[TypeRelation]) => TypeRelation (
                                t1.subType, t2.superType
                            )
                        ) FROM(rec, rec) WHERE (
                            _.superType == _.subType
                            )
                    )
                )
            )


    lazy val constructors: Rep[Query[MethodDeclaration]] =
        SELECT (*) FROM methodDeclarations WHERE (_.name == "<init>")
}