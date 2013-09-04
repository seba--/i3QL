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
 *
 */

object CN_IDIOM
{
    def apply (database: BytecodeDatabase): Relation[database.ObjectType] = {
        import database._
        //val Cloneable = ObjectType ("java/lang/Cloneable")
        //val Object = ObjectType ("java/lang/Object")

        //val subTyping1 = BagExtent.empty[TypeRelation]
        //SELECT ((_: Rep[TypeRelation]).subType) FROM subTyping
        val printer = new RelationalAlgebraPrintPlan
        {
            val IR = idb.syntax.iql.IR
        }

        val clause =
            SELECT ((_: Rep[TypeRelation]).subType) FROM subTyping WHERE ((t: Rep[TypeRelation]) =>
                t.superType == ObjectType ("java/lang/Cloneable") AND
                    NOT (
                        EXISTS (
                            SELECT (*) FROM methodDeclarations WHERE ((m: Rep[MethodDeclaration]) =>
                                m.name == "clone" AND
                                    m.returnType == ObjectType ("java/lang/Object") AND
                                    m.parameterTypes == Nil AND
                                    m.receiverType == t.subType
                                )
                        )
                    )
                )

        //val query = plan(clause)
        //Predef.println(printer.quoteRelation (query))
        clause
    }

}


/*

projection(
    difference(
        selection(
            RecursionResult(
                projection(
                    equiJoin(
                        Recursion(
                            unionAdd(
                                projection(
                                    selection(
                                        extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                        (x16: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                            val x17 = x16.superType
                                            val x18 = x17.isDefined
                                            x18
                                        }
                                    )[ref=Sym(20)],
                                    (x9: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                        val x2 = px2 // static data: <function1>
                                        val x10 = x9.superType
                                        val x11 = x10.get
                                        val x13 = x2(x9,x11)
                                        x13
                                    }
                                )[ref=Sym(21)],
                                projection(
                                    unnest(
                                        extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                        (x28: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                            val x29 = x28.interfaces
                                            x29
                                        }
                                    )[ref=Sym(31)],
                                    ((x22:sae.bytecode.asm.structure.ClassDeclaration,x23:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                        val x2 = px2 // static data: <function1>
                                        val x25 = x2(x22,x23)
                                        x25
                                    }
                                )[ref=Sym(32)]
                            )[ref=Sym(33)],
                            Sym(54)
                        ),
                        Recursion(
                            unionAdd(
                                projection(
                                    selection(
                                        extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                        (x16: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                            val x17 = x16.superType
                                            val x18 = x17.isDefined
                                            x18
                                        }
                                    )[ref=Sym(20)],
                                    (x9: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                        val x2 = px2 // static data: <function1>
                                        val x10 = x9.superType
                                        val x11 = x10.get
                                        val x13 = x2(x9,x11)
                                        x13
                                    }
                                )[ref=Sym(21)],
                                projection(
                                    unnest(
                                        extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                        (x28: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                            val x29 = x28.interfaces
                                            x29
                                        }
                                    )[ref=Sym(31)],
                                    ((x22:sae.bytecode.asm.structure.ClassDeclaration,x23:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                        val x2 = px2 // static data: <function1>
                                        val x25 = x2(x22,x23)
                                        x25
                                    }
                                )[ref=Sym(32)]
                            )[ref=Sym(33)],
                            Sym(54)
                        ),
                        Seq(
                            (
                            (x35: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                val x37 = x35.superType
                                x37
                            },
                            (x3: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                val x4 = x3.subType
                                x4
                            }
                            )
                        )
                    )[ref=Sym(43)],
                    ((x44:sae.bytecode.asm.structure.TypeRelation,x45:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.TypeRelation => {
                        val x1 = px1 // static data: <function1>
                        val x46 = x44.subType
                        val x47 = x45.superType
                        val x49 = x1(x46,x47)
                        x49
                    }
                )[ref=Sym(52)]
            ),
            (x55: sae.bytecode.asm.structure.TypeRelation): Boolean => {
                val x56 = x55.superType
                val x0 = px0 // static data: <function1>
                val x57 = x0("java/lang/Cloneable")
                val x58 = x56 == x57
                x58
            }
        )[ref=Sym(211)],
        projection(
            equiJoin(
                selection(
                    RecursionResult(
                        projection(
                            equiJoin(
                                Recursion(
                                    unionAdd(
                                        projection(
                                            selection(
                                                extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                                (x16: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                    val x17 = x16.superType
                                                    val x18 = x17.isDefined
                                                    x18
                                                }
                                            )[ref=Sym(20)],
                                            (x9: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                val x2 = px2 // static data: <function1>
                                                val x10 = x9.superType
                                                val x11 = x10.get
                                                val x13 = x2(x9,x11)
                                                x13
                                            }
                                        )[ref=Sym(21)],
                                        projection(
                                            unnest(
                                                extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                                (x28: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                    val x29 = x28.interfaces
                                                    x29
                                                }
                                            )[ref=Sym(31)],
                                            ((x22:sae.bytecode.asm.structure.ClassDeclaration,x23:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                val x2 = px2 // static data: <function1>
                                                val x25 = x2(x22,x23)
                                                x25
                                            }
                                        )[ref=Sym(32)]
                                    )[ref=Sym(33)],
                                    Sym(54)
                                ),
                                Recursion(
                                    unionAdd(
                                        projection(
                                            selection(
                                                extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                                (x16: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                    val x17 = x16.superType
                                                    val x18 = x17.isDefined
                                                    x18
                                                }
                                            )[ref=Sym(20)],
                                            (x9: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                val x2 = px2 // static data: <function1>
                                                val x10 = x9.superType
                                                val x11 = x10.get
                                                val x13 = x2(x9,x11)
                                                x13
                                            }
                                        )[ref=Sym(21)],
                                        projection(
                                            unnest(
                                                extent1174847245: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@4606bf0d,true,false,false)],
                                                (x28: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                    val x29 = x28.interfaces
                                                    x29
                                                }
                                            )[ref=Sym(31)],
                                            ((x22:sae.bytecode.asm.structure.ClassDeclaration,x23:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                val x2 = px2 // static data: <function1>
                                                val x25 = x2(x22,x23)
                                                x25
                                            }
                                        )[ref=Sym(32)]
                                    )[ref=Sym(33)],
                                    Sym(54)
                                ),
                                Seq(
                                    (
                                    (x35: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                        val x37 = x35.superType
                                        x37
                                    },
                                    (x3: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                        val x4 = x3.subType
                                        x4
                                    }
                                    )
                                )
                            )[ref=Sym(43)],
                            ((x44:sae.bytecode.asm.structure.TypeRelation,x45:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.TypeRelation => {
                                val x1 = px1 // static data: <function1>
                                val x46 = x44.subType
                                val x47 = x45.superType
                                val x49 = x1(x46,x47)
                                x49
                            }
                        )[ref=Sym(52)]
                    ),
                    (x212: sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                        val x0 = px0 // static data: <function1>
                        val x215 = x0("java/lang/Cloneable")
                        val x214 = x212.superType
                        val x216 = x214 == x215
                        x216
                    }
                )[ref=Sym(220)],
                duplicateElimination(
                    projection(
                        selection(
                            extent1503504810: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@599da9aa,true,false,false)],
                            (x140: sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                                val x143 = x140.parameterTypes
                                val x144 = x143 == List()
                                val x141 = x140.name
                                val x142 = x141 == "clone"
                                val x0 = px0 // static data: <function1>
                                val x146 = x0("java/lang/Object")
                                val x147 = x140.returnType
                                val x148 = x147 == x146
                                val x151 = x142 && x149
                                val x152 = x144 && x151
                                x152
                            }
                        )[ref=Sym(154)],
                        (x93: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): org.objectweb.asm.Type => {
                            val x111 = x93.receiverType
                            x111
                        }
                    )[ref=Sym(165)]
                )[ref=Sym(166)],
                Seq(
                    (
                    (x3: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                        val x4 = x3.subType
                        x4
                    },
                    (x167: Any): Any => {
                        x167
                    }
                    )
                )
            )[ref=Sym(221)],
            ((x170:sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation,x171:Any): sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation => {
                x170
            }
        )[ref=Sym(222)]
    )[ref=Sym(230)],
    (x3: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
        val x4 = x3.subType
        x4
    }
)[ref=Sym(231)]

 */
