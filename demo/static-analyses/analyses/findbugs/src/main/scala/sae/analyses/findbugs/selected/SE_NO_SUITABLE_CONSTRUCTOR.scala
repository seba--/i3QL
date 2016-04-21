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
package sae.analyses.findbugs.selected

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.algebra.print.RelationalAlgebraPrintPlan

/**
 *
 * @author Ralf Mitschke
 */
object SE_NO_SUITABLE_CONSTRUCTOR
	extends (BytecodeDatabase => Relation[BytecodeDatabase#ObjectType])
{
    def apply (database: BytecodeDatabase): Relation[BytecodeDatabase#ObjectType] = {
        import database._

        val superTypeDefined : Relation[ClassDeclaration] =
            SELECT (*) FROM classDeclarations WHERE ((c : Rep[ClassDeclaration]) => c.superType.isDefined)

        val query =
            SELECT (
                (_: Rep[ClassDeclaration]).superType.get) FROM superTypeDefined WHERE ((c: Rep[ClassDeclaration]) =>
                NOT (c.isInterface) AND
                    EXISTS (
                        SELECT (*) FROM classDeclarations WHERE ((c1: Rep[ClassDeclaration]) =>
                            NOT (c1.isInterface) AND
                                c1.classType == c.superType.get
                            )
                    ) AND
                    EXISTS (
                        SELECT (*) FROM interfaceInheritance WHERE ((in: Rep[Inheritance]) =>
                            in.superType == ObjectType ("java/io/Serializable") AND
                                in.declaringClass == c
                            )
                    ) AND
                    NOT (
                        EXISTS (
                            SELECT (*) FROM constructors WHERE ((m: Rep[MethodDeclaration]) =>
                                m.parameterTypes == Nil AND
                                    m.declaringType == c.superType.get
                                )
                        )
                    )
                )

        query
    }
}


/*

projection(
    difference(
        projection(
            equiJoin(
                equiJoin(
                    selection(
                        relation1372854628: Relation[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryRelation
                        (idb.SetExtent@51d41964,true,false,false)],
                        (x8: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                            val x9 = x8.isInterface
                            val x10 = !x9
                            val x11 = x8.superType
                            val x12 = x11.isDefined
                            val x13 = x10 && x12
                            x13
                        }
                    )[ref=Sym(83)],
                    duplicateElimination(
                        projection(
                            selection(
                                relation1372854628: Relation[sae.bytecode.asm.structure
                                .ClassDeclaration][ref=QueryRelation(idb.SetExtent@51d41964,true,false,false)],
                                (x8: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                    val x9 = x8.isInterface
                                    val x10 = !x9
                                    x10
                                }
                            )[ref=Sym(74)],
                            (x89: sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm.structure
                            .ClassDeclaration): org.objectweb.asm.Type => {
                                val x97 = x89.classType
                                x97
                            }
                        )[ref=Sym(113)]
                    )[ref=Sym(114)],
                    Seq(
                        (
                        (x4: sae.bytecode.asm.structure.ClassDeclaration): org.objectweb.asm.Type => {
                            val x5 = x4.superType
                            val x6 = x5.get
                            x6
                        },
                        (x115: Any): Any => {
                            x115
                        }
                        )
                    )
                )[ref=Sym(152)],
                duplicateElimination(
                    projection(
                        equiJoin(
                            equiJoin(
                                selection(
                                    relation1372854628: Relation[sae.bytecode.asm.structure
                                    .ClassDeclaration][ref=QueryRelation(idb.SetExtent@51d41964,true,false,false)],
                                    (x8: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                        val x9 = x8.isInterface
                                        val x10 = !x9
                                        val x11 = x8.superType
                                        val x12 = x11.isDefined
                                        val x13 = x10 && x12
                                        x13
                                    }
                                )[ref=Sym(83)],
                                duplicateElimination(
                                    projection(
                                        selection(
                                            relation1372854628: Relation[sae.bytecode.asm.structure
                                            .ClassDeclaration][ref=QueryRelation(idb.SetExtent@51d41964,true,false,
                                            false)],
                                            (x8: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                val x9 = x8.isInterface
                                                val x10 = !x9
                                                x10
                                            }
                                        )[ref=Sym(74)],
                                        (x89: sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm
                                        .structure.ClassDeclaration): org.objectweb.asm.Type => {
                                            val x97 = x89.classType
                                            x97
                                        }
                                    )[ref=Sym(113)]
                                )[ref=Sym(114)],
                                Seq(
                                    (
                                    (x4: sae.bytecode.asm.structure.ClassDeclaration): org.objectweb.asm.Type => {
                                        val x5 = x4.superType
                                        val x6 = x5.get
                                        x6
                                    },
                                    (x115: Any): Any => {
                                        x115
                                    }
                                    )
                                )
                            )[ref=Sym(152)],
                            selection(
                                unnest(
                                    relation1372854628: Relation[sae.bytecode.asm.structure
                                    .ClassDeclaration][ref=QueryRelation(idb.SetExtent@51d41964,true,false,false)],
                                    (x32: sae.bytecode.asm.structure.ClassDeclaration): scala.collection
                                    .Traversable[org.objectweb.asm.Type] => {
                                        val x33 = x32.interfaces
                                        x33
                                    }
                                )[ref=Sym(35)],
                                (x185: scala.Tuple2[sae.bytecode.asm.structure.ClassDeclaration,
                                org.objectweb.asm.Type]): Boolean => {
                                    val x1 = px1 // static data: <function1>
                                    val x41 = x1("java/io/Serializable")
                                    val x3 = px3 // static data: <function1>
                                    val x187 = x185._1
                                    val x188 = x185._2
                                    val x189 = x3(x187,x188)
                                    val x191 = x189.superType
                                    val x192 = x191 == x41
                                    x192
                                }
                            )[ref=Sym(195)],
                            Seq(
                                (
                                (x135: scala.Tuple2[sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm
                                .structure.ClassDeclaration, Any]): sae.bytecode.asm.structure.ClassDeclaration with
                                sae.bytecode.asm.structure.ClassDeclaration => {
                                    val x136 = x135._1
                                    x136
                                },
                                (x200: scala.Tuple2[sae.bytecode.asm.structure.ClassDeclaration,
                                org.objectweb.asm.Type]): sae.bytecode.asm.structure.ClassDeclaration => {
                                    val x3 = px3 // static data: <function1>
                                    val x202 = x200._1
                                    val x203 = x200._2
                                    val x204 = x3(x202,x203)
                                    val x206 = x204.declaringClass
                                    x206
                                }
                                )
                            )
                        )[ref=Sym(211)],
                        ((x216:scala.Tuple2[sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm
                        .structure.ClassDeclaration, Any],x217:scala.Tuple2[sae.bytecode.asm.structure
                        .ClassDeclaration, org.objectweb.asm.Type]): sae.bytecode.asm.structure.ClassDeclaration => {
                            val x218 = x216._1
                            x218
                        }
                    )[ref=Sym(224)]
                )[ref=Sym(225)],
                Seq(
                    (
                    (x135: scala.Tuple2[sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm.structure
                    .ClassDeclaration, Any]): sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm
                    .structure.ClassDeclaration => {
                        val x136 = x135._1
                        x136
                    },
                    (x14: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.ClassDeclaration => {
                        x14
                    }
                    )
                )
            )[ref=Sym(246)],
            ((x251:scala.Tuple2[sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm.structure
            .ClassDeclaration, Any],x252:sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm.structure
            .ClassDeclaration): sae.bytecode.asm.structure.ClassDeclaration => {
                val x253 = x251._1
                x253
            }
        )[ref=Sym(256)],
        projection(
            equiJoin(
                relation1372854628: Relation[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryRelation(idb
                .SetExtent@51d41964,true,false,false)],
                duplicateElimination(
                    projection(
                        selection(
                            relation1361247491: Relation[sae.bytecode.asm.structure
                            .MethodDeclaration][ref=QueryRelation(idb.SetExtent@5122fd03,true,false,false)],
                            (x276: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure
                            .MethodDeclaration): Boolean => {
                                val x277 = x276.name
                                val x278 = x277 == "<init>"
                                val x279 = x276.parameterTypes
                                val x280 = x279 == List()
                                val x281 = x278 && x280
                                x281
                            }
                        )[ref=Sym(283)],
                        (x262: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure
                        .MethodDeclaration): org.objectweb.asm.Type => {
                            val x270 = x262.declaringType
                            x270
                        }
                    )[ref=Sym(294)]
                )[ref=Sym(295)],
                Seq(
                    (
                    (x4: sae.bytecode.asm.structure.ClassDeclaration): org.objectweb.asm.Type => {
                        val x5 = x4.superType
                        val x6 = x5.get
                        x6
                    },
                    (x115: Any): Any => {
                        x115
                    }
                    )
                )
            )[ref=Sym(332)],
            ((x118:sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm.structure.ClassDeclaration,x119:Any): sae.bytecode.asm.structure.ClassDeclaration with sae.bytecode.asm.structure.ClassDeclaration => {
                x118
            }
        )[ref=Sym(336)]
    )[ref=Sym(337)],
    (x4: sae.bytecode.asm.structure.ClassDeclaration): org.objectweb.asm.Type => {
        val x5 = x4.superType
        val x6 = x5.get
        x6
    }
)[ref=Sym(338)]

*/