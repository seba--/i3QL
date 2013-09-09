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
object CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
{
    def apply (database: BytecodeDatabase): Relation[Any] = {
        import database._

        val query =
        SELECT (*) FROM methodDeclarations WHERE ((m: Rep[MethodDeclaration]) =>
            NOT (m.isAbstract) AND
                m.name == "clone" AND
                m.parameterTypes == Nil AND
                m.returnType == ObjectType ("java/lang/Object") AND
                m.declaringClass.superType.isDefined AND
                NOT (
                    EXISTS (
                        SELECT (*) FROM subTyping WHERE ((t: Rep[TypeRelation]) =>
                            t.superType == ObjectType ("java/lang/Cloneable") AND
                            t.subType == m.declaringType
                            )
                    )
                )
            )

        val printer = new RelationalAlgebraPrintPlan
        {
            val IR = idb.syntax.iql.IR
        }

        Predef.println(printer.quoteRelation(query))

        //SELECT (*) FROM subTyping WHERE (_.subType == ObjectType("javax/swing/text/StyledEditorKit"))
        query
    }
}


/*


difference(
    difference(
        selection(
            extent23304757: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@1639a35,true,false,false)],
            (x317: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                val x0 = px0 // static data: <function1>
                val x319 = x0("java/lang/Object")
                val x324 = x317.parameterTypes
                val x325 = x324 == List()
                val x321 = x317.declaringClass
                val x322 = x321.superType
                val x323 = x322.isDefined
                val x318 = x317.returnType
                val x320 = x318 == x319
                val x330 = x317.name
                val x331 = x330 == "clone"
                val x333 = x320 && x331
                val x335 = x323 && x333
                val x336 = x325 && x335
                x336
            }
        )[ref=Sym(339)],
        projection(
            equiJoin(
                selection(
                    extent23304757: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@1639a35,true,false,false)],
                    (x401: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                        val x0 = px0 // static data: <function1>
                        val x403 = x0("java/lang/Object")
                        val x410 = x0("java/lang/Object")
                        val x429 = x0("java/lang/Object")
                        val x402 = x401.returnType
                        val x430 = x402 == x429
                        val x412 = x401.name
                        val x413 = x412 == "clone"
                        val x411 = x402 == x410
                        val x408 = x401.parameterTypes
                        val x409 = x408 == List()
                        val x405 = x401.declaringClass
                        val x406 = x405.superType
                        val x407 = x406.isDefined
                        val x404 = x402 == x403
                        val x423 = x404 && x413
                        val x424 = x407 && x423
                        val x425 = x407 && x424
                        val x440 = x407 && x425
                        val x441 = x409 && x440
                        val x444 = x409 && x441
                        val x447 = x411 && x444
                        val x448 = x413 && x447
                        val x449 = x430 && x448
                        x449
                    }
                )[ref=Sym(452)],
                duplicateElimination(
                    projection(
                        selection(
                            crossProduct(
                                extent23304757: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@1639a35,true,false,false)],
                                selection(
                                    duplicateElimination(
                                        RecursionResult(
                                            unionAdd(
                                                unionAdd(
                                                    projection(
                                                        selection(
                                                            extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                            (x38: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                val x39 = x38.superType
                                                                val x40 = x39.isDefined
                                                                x40
                                                            }
                                                        )[ref=Sym(42)],
                                                        (x31: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                            val x2 = px2 // static data: <function1>
                                                            val x32 = x31.superType
                                                            val x33 = x32.get
                                                            val x35 = x2(x31,x33)
                                                            x35
                                                        }
                                                    )[ref=Sym(43)],
                                                    projection(
                                                        unnest(
                                                            extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                            (x50: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                val x51 = x50.interfaces
                                                                x51
                                                            }
                                                        )[ref=Sym(53)],
                                                        ((x44:sae.bytecode.asm.structure.ClassDeclaration,x45:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                            val x2 = px2 // static data: <function1>
                                                            val x47 = x2(x44,x45)
                                                            x47
                                                        }
                                                    )[ref=Sym(54)]
                                                )[ref=Sym(55)],
                                                projection(
                                                    equiJoin(
                                                        Recursion(
                                                            unionAdd(
                                                                projection(
                                                                    selection(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x38: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                            val x39 = x38.superType
                                                                            val x40 = x39.isDefined
                                                                            x40
                                                                        }
                                                                    )[ref=Sym(42)],
                                                                    (x31: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x32 = x31.superType
                                                                        val x33 = x32.get
                                                                        val x35 = x2(x31,x33)
                                                                        x35
                                                                    }
                                                                )[ref=Sym(43)],
                                                                projection(
                                                                    unnest(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x50: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                            val x51 = x50.interfaces
                                                                            x51
                                                                        }
                                                                    )[ref=Sym(53)],
                                                                    ((x44:sae.bytecode.asm.structure.ClassDeclaration,x45:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x47 = x2(x44,x45)
                                                                        x47
                                                                    }
                                                                )[ref=Sym(54)]
                                                            )[ref=Sym(55)],
                                                            Sym(76)
                                                        ),
                                                        Recursion(
                                                            unionAdd(
                                                                projection(
                                                                    selection(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x38: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                            val x39 = x38.superType
                                                                            val x40 = x39.isDefined
                                                                            x40
                                                                        }
                                                                    )[ref=Sym(42)],
                                                                    (x31: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x32 = x31.superType
                                                                        val x33 = x32.get
                                                                        val x35 = x2(x31,x33)
                                                                        x35
                                                                    }
                                                                )[ref=Sym(43)],
                                                                projection(
                                                                    unnest(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x50: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                            val x51 = x50.interfaces
                                                                            x51
                                                                        }
                                                                    )[ref=Sym(53)],
                                                                    ((x44:sae.bytecode.asm.structure.ClassDeclaration,x45:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x47 = x2(x44,x45)
                                                                        x47
                                                                    }
                                                                )[ref=Sym(54)]
                                                            )[ref=Sym(55)],
                                                            Sym(76)
                                                        ),
                                                        Seq(
                                                            (
                                                            (x64: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                                val x66 = x64.superType
                                                                x66
                                                            },
                                                            (x65: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                                val x67 = x65.subType
                                                                x67
                                                            }
                                                            )
                                                        )
                                                    )[ref=Sym(73)],
                                                    ((x56:sae.bytecode.asm.structure.TypeRelation,x57:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.TypeRelation => {
                                                        val x1 = px1 // static data: <function1>
                                                        val x58 = x56.subType
                                                        val x59 = x57.superType
                                                        val x61 = x1(x58,x59)
                                                        x61
                                                    }
                                                )[ref=Sym(74)]
                                            )[ref=Sym(75)]
                                        )
                                    )[ref=Sym(79)],
                                    (x107: sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                                        val x0 = px0 // static data: <function1>
                                        val x116 = x0("java/lang/Cloneable")
                                        val x115 = x107.superType
                                        val x117 = x115 == x116
                                        x117
                                    }
                                )[ref=Sym(126)]
                            )[ref=Sym(127)],
                            ((x106:sae.bytecode.asm.structure.MethodDeclaration,x107:sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                                val x0 = px0 // static data: <function1>
                                val x116 = x0("java/lang/Cloneable")
                                val x108 = x106.declaringType
                                val x118 = x107.subType
                                val x119 = x118 == x108
                                x119
                            }
                        )[ref=Sym(130)],
                        ((x131:sae.bytecode.asm.structure.MethodDeclaration,x132:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.MethodDeclaration => {
                            x131
                        }
                    )[ref=Sym(134)]
                )[ref=Sym(135)],
                Seq(
                    (
                    (x3: sae.bytecode.asm.structure.MethodDeclaration): sae.bytecode.asm.structure.MethodDeclaration => {
                        x3
                    },
                    (x3: sae.bytecode.asm.structure.MethodDeclaration): sae.bytecode.asm.structure.MethodDeclaration => {
                        x3
                    }
                    )
                )
            )[ref=Sym(453)],
            ((x144:sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration,x145:sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration => {
                x144
            }
        )[ref=Sym(454)]
    )[ref=Sym(455)],
    difference(
        selection(
            extent23304757: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@1639a35,true,false,false)],
            (x458: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                val x0 = px0 // static data: <function1>
                val x460 = x0("java/lang/Object")
                val x465 = x458.parameterTypes
                val x466 = x465 == List()
                val x462 = x458.declaringClass
                val x463 = x462.superType
                val x464 = x463.isDefined
                val x459 = x458.returnType
                val x461 = x459 == x460
                val x471 = x458.isAbstract
                val x473 = x461 && x471
                val x475 = x464 && x473
                val x476 = x466 && x475
                x476
            }
        )[ref=Sym(479)],
        projection(
            equiJoin(
                selection(
                    extent23304757: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@1639a35,true,false,false)],
                    (x538: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                        val x0 = px0 // static data: <function1>
                        val x540 = x0("java/lang/Object")
                        val x547 = x0("java/lang/Object")
                        val x565 = x0("java/lang/Object")
                        val x539 = x538.returnType
                        val x566 = x539 == x565
                        val x549 = x538.isAbstract
                        val x548 = x539 == x547
                        val x545 = x538.parameterTypes
                        val x546 = x545 == List()
                        val x542 = x538.declaringClass
                        val x543 = x542.superType
                        val x544 = x543.isDefined
                        val x541 = x539 == x540
                        val x559 = x541 && x549
                        val x560 = x544 && x559
                        val x561 = x544 && x560
                        val x576 = x544 && x561
                        val x577 = x546 && x576
                        val x580 = x546 && x577
                        val x583 = x548 && x580
                        val x584 = x549 && x583
                        val x585 = x566 && x584
                        x585
                    }
                )[ref=Sym(588)],
                duplicateElimination(
                    projection(
                        selection(
                            crossProduct(
                                extent23304757: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@1639a35,true,false,false)],
                                selection(
                                    duplicateElimination(
                                        RecursionResult(
                                            unionAdd(
                                                unionAdd(
                                                    projection(
                                                        selection(
                                                            extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                            (x38: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                val x39 = x38.superType
                                                                val x40 = x39.isDefined
                                                                x40
                                                            }
                                                        )[ref=Sym(42)],
                                                        (x31: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                            val x2 = px2 // static data: <function1>
                                                            val x32 = x31.superType
                                                            val x33 = x32.get
                                                            val x35 = x2(x31,x33)
                                                            x35
                                                        }
                                                    )[ref=Sym(43)],
                                                    projection(
                                                        unnest(
                                                            extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                            (x50: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                val x51 = x50.interfaces
                                                                x51
                                                            }
                                                        )[ref=Sym(53)],
                                                        ((x44:sae.bytecode.asm.structure.ClassDeclaration,x45:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                            val x2 = px2 // static data: <function1>
                                                            val x47 = x2(x44,x45)
                                                            x47
                                                        }
                                                    )[ref=Sym(54)]
                                                )[ref=Sym(55)],
                                                projection(
                                                    equiJoin(
                                                        Recursion(
                                                            unionAdd(
                                                                projection(
                                                                    selection(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x38: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                            val x39 = x38.superType
                                                                            val x40 = x39.isDefined
                                                                            x40
                                                                        }
                                                                    )[ref=Sym(42)],
                                                                    (x31: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x32 = x31.superType
                                                                        val x33 = x32.get
                                                                        val x35 = x2(x31,x33)
                                                                        x35
                                                                    }
                                                                )[ref=Sym(43)],
                                                                projection(
                                                                    unnest(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x50: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                            val x51 = x50.interfaces
                                                                            x51
                                                                        }
                                                                    )[ref=Sym(53)],
                                                                    ((x44:sae.bytecode.asm.structure.ClassDeclaration,x45:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x47 = x2(x44,x45)
                                                                        x47
                                                                    }
                                                                )[ref=Sym(54)]
                                                            )[ref=Sym(55)],
                                                            Sym(76)
                                                        ),
                                                        Recursion(
                                                            unionAdd(
                                                                projection(
                                                                    selection(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x38: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                            val x39 = x38.superType
                                                                            val x40 = x39.isDefined
                                                                            x40
                                                                        }
                                                                    )[ref=Sym(42)],
                                                                    (x31: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x32 = x31.superType
                                                                        val x33 = x32.get
                                                                        val x35 = x2(x31,x33)
                                                                        x35
                                                                    }
                                                                )[ref=Sym(43)],
                                                                projection(
                                                                    unnest(
                                                                        extent3515826: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@35a5b2,true,false,false)],
                                                                        (x50: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                            val x51 = x50.interfaces
                                                                            x51
                                                                        }
                                                                    )[ref=Sym(53)],
                                                                    ((x44:sae.bytecode.asm.structure.ClassDeclaration,x45:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                        val x2 = px2 // static data: <function1>
                                                                        val x47 = x2(x44,x45)
                                                                        x47
                                                                    }
                                                                )[ref=Sym(54)]
                                                            )[ref=Sym(55)],
                                                            Sym(76)
                                                        ),
                                                        Seq(
                                                            (
                                                            (x64: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                                val x66 = x64.superType
                                                                x66
                                                            },
                                                            (x65: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                                val x67 = x65.subType
                                                                x67
                                                            }
                                                            )
                                                        )
                                                    )[ref=Sym(73)],
                                                    ((x56:sae.bytecode.asm.structure.TypeRelation,x57:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.TypeRelation => {
                                                        val x1 = px1 // static data: <function1>
                                                        val x58 = x56.subType
                                                        val x59 = x57.superType
                                                        val x61 = x1(x58,x59)
                                                        x61
                                                    }
                                                )[ref=Sym(74)]
                                            )[ref=Sym(75)]
                                        )
                                    )[ref=Sym(79)],
                                    (x107: sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                                        val x0 = px0 // static data: <function1>
                                        val x116 = x0("java/lang/Cloneable")
                                        val x115 = x107.superType
                                        val x117 = x115 == x116
                                        x117
                                    }
                                )[ref=Sym(126)]
                            )[ref=Sym(127)],
                            ((x106:sae.bytecode.asm.structure.MethodDeclaration,x107:sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                                val x0 = px0 // static data: <function1>
                                val x116 = x0("java/lang/Cloneable")
                                val x108 = x106.declaringType
                                val x118 = x107.subType
                                val x119 = x118 == x108
                                x119
                            }
                        )[ref=Sym(130)],
                        ((x131:sae.bytecode.asm.structure.MethodDeclaration,x132:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.MethodDeclaration => {
                            x131
                        }
                    )[ref=Sym(134)]
                )[ref=Sym(135)],
                Seq(
                    (
                    (x3: sae.bytecode.asm.structure.MethodDeclaration): sae.bytecode.asm.structure.MethodDeclaration => {
                        x3
                    },
                    (x3: sae.bytecode.asm.structure.MethodDeclaration): sae.bytecode.asm.structure.MethodDeclaration => {
                        x3
                    }
                    )
                )
            )[ref=Sym(589)],
            ((x144:sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration,x145:sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration => {
                x144
            }
        )[ref=Sym(590)]
    )[ref=Sym(591)]
)[ref=Sym(592)]

*/