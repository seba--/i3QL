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

        query
    }
}


/*

// TODO need some optimizations
difference(
    difference(
        selection(
            extent2078117529: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@7bdd8e99,true,false,false)],
            (x987: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                val x668 = px668 // static data: <function1>
                val x684 = x668("java/lang/Object")
                val x993 = x987.parameterTypes
                val x994 = x993 == List()
                val x990 = x987.declaringClass
                val x991 = x990.superType
                val x992 = x991.isDefined
                val x988 = x987.returnType
                val x989 = x988 == x684
                val x998 = x987.name
                val x999 = x998 == "clone"
                val x1001 = x989 && x999
                val x1003 = x992 && x1001
                val x1004 = x994 && x1003
                x1004
            }
        )[ref=Sym(1007)],
        projection(
            equiJoin(
                selection(
                    extent2078117529: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@7bdd8e99,true,false,false)],
                    (x1062: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                        val x668 = px668 // static data: <function1>
                        val x684 = x668("java/lang/Object")
                        val x1070 = x1062.name
                        val x1071 = x1070 == "clone"
                        val x1068 = x1062.parameterTypes
                        val x1069 = x1068 == List()
                        val x1065 = x1062.declaringClass
                        val x1066 = x1065.superType
                        val x1067 = x1066.isDefined
                        val x1063 = x1062.returnType
                        val x1064 = x1063 == x684
                        val x1077 = x1064 && x1071
                        val x1081 = x1064 && x1077
                        val x1089 = x1064 && x1081
                        val x1090 = x1067 && x1089
                        val x1093 = x1067 && x1090
                        val x1096 = x1067 && x1093
                        val x1097 = x1069 && x1096
                        val x1099 = x1069 && x1097
                        val x1100 = x1071 && x1099
                        x1100
                    }
                )[ref=Sym(1103)],
                duplicateElimination(
                    projection(
                        selection(
                            duplicateElimination(
                                RecursionResult(
                                    unionAdd(
                                        unionAdd(
                                            projection(
                                                selection(
                                                    extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                    (x42: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                        val x43 = x42.superType
                                                        val x44 = x43.isDefined
                                                        x44
                                                    }
                                                )[ref=Sym(710)],
                                                (x699: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                    val x670 = px670 // static data: <function1>
                                                    val x700 = x699.superType
                                                    val x701 = x700.get
                                                    val x703 = x670(x699,x701)
                                                    x703
                                                }
                                            )[ref=Sym(711)],
                                            projection(
                                                unnest(
                                                    extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                    (x54: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                        val x55 = x54.interfaces
                                                        x55
                                                    }
                                                )[ref=Sym(721)],
                                                ((x712:sae.bytecode.asm.structure.ClassDeclaration,x713:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                    val x670 = px670 // static data: <function1>
                                                    val x715 = x670(x712,x713)
                                                    x715
                                                }
                                            )[ref=Sym(722)]
                                        )[ref=Sym(723)],
                                        projection(
                                            equiJoin(
                                                Recursion(
                                                    unionAdd(
                                                        projection(
                                                            selection(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x42: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                    val x43 = x42.superType
                                                                    val x44 = x43.isDefined
                                                                    x44
                                                                }
                                                            )[ref=Sym(710)],
                                                            (x699: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x700 = x699.superType
                                                                val x701 = x700.get
                                                                val x703 = x670(x699,x701)
                                                                x703
                                                            }
                                                        )[ref=Sym(711)],
                                                        projection(
                                                            unnest(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x54: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                    val x55 = x54.interfaces
                                                                    x55
                                                                }
                                                            )[ref=Sym(721)],
                                                            ((x712:sae.bytecode.asm.structure.ClassDeclaration,x713:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x715 = x670(x712,x713)
                                                                x715
                                                            }
                                                        )[ref=Sym(722)]
                                                    )[ref=Sym(723)],
                                                    Sym(743)
                                                ),
                                                Recursion(
                                                    unionAdd(
                                                        projection(
                                                            selection(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x42: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                    val x43 = x42.superType
                                                                    val x44 = x43.isDefined
                                                                    x44
                                                                }
                                                            )[ref=Sym(710)],
                                                            (x699: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x700 = x699.superType
                                                                val x701 = x700.get
                                                                val x703 = x670(x699,x701)
                                                                x703
                                                            }
                                                        )[ref=Sym(711)],
                                                        projection(
                                                            unnest(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x54: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                    val x55 = x54.interfaces
                                                                    x55
                                                                }
                                                            )[ref=Sym(721)],
                                                            ((x712:sae.bytecode.asm.structure.ClassDeclaration,x713:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x715 = x670(x712,x713)
                                                                x715
                                                            }
                                                        )[ref=Sym(722)]
                                                    )[ref=Sym(723)],
                                                    Sym(743)
                                                ),
                                                Seq(
                                                    (
                                                    (x68: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                        val x70 = x68.superType
                                                        x70
                                                    },
                                                    (x29: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                        val x30 = x29.subType
                                                        x30
                                                    }
                                                    )
                                                )
                                            )[ref=Sym(740)],
                                            ((x724:sae.bytecode.asm.structure.TypeRelation,x725:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.TypeRelation => {
                                                val x669 = px669 // static data: <function1>
                                                val x726 = x724.subType
                                                val x727 = x725.superType
                                                val x729 = x669(x726,x727)
                                                x729
                                            }
                                        )[ref=Sym(741)]
                                    )[ref=Sym(742)]
                                )
                            )[ref=Sym(746)],
                            (x774: sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                                val x668 = px668 // static data: <function1>
                                val x751 = x668("java/lang/Cloneable")
                                val x780 = x774.superType
                                val x781 = x780 == x751
                                x781
                            }
                        )[ref=Sym(788)],
                        (x29: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                            val x30 = x29.subType
                            x30
                        }
                    )[ref=Sym(799)]
                )[ref=Sym(800)],
                Seq(
                    (
                    (x123: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): org.objectweb.asm.Type => {
                        val x133 = x123.declaringType
                        x133
                    },
                    (x192: Any): Any => {
                        x192
                    }
                    )
                )
            )[ref=Sym(1104)],
            ((x804:sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration,x805:Any): sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration => {
                x804
            }
        )[ref=Sym(1105)]
    )[ref=Sym(1106)],
    difference(
        selection(
            extent2078117529: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@7bdd8e99,true,false,false)],
            (x1107: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                val x668 = px668 // static data: <function1>
                val x684 = x668("java/lang/Object")
                val x1113 = x1107.parameterTypes
                val x1114 = x1113 == List()
                val x1110 = x1107.declaringClass
                val x1111 = x1110.superType
                val x1112 = x1111.isDefined
                val x1108 = x1107.returnType
                val x1109 = x1108 == x684
                val x1118 = x1107.isAbstract
                val x1120 = x1109 && x1118
                val x1122 = x1112 && x1120
                val x1123 = x1114 && x1122
                x1123
            }
        )[ref=Sym(1126)],
        projection(
            equiJoin(
                selection(
                    extent2078117529: Extent[sae.bytecode.asm.structure.MethodDeclaration][ref=QueryExtent(idb.SetExtent@7bdd8e99,true,false,false)],
                    (x1178: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): Boolean => {
                        val x668 = px668 // static data: <function1>
                        val x684 = x668("java/lang/Object")
                        val x1186 = x1178.isAbstract
                        val x1184 = x1178.parameterTypes
                        val x1185 = x1184 == List()
                        val x1181 = x1178.declaringClass
                        val x1182 = x1181.superType
                        val x1183 = x1182.isDefined
                        val x1179 = x1178.returnType
                        val x1180 = x1179 == x684
                        val x1192 = x1180 && x1186
                        val x1196 = x1180 && x1192
                        val x1204 = x1180 && x1196
                        val x1205 = x1183 && x1204
                        val x1208 = x1183 && x1205
                        val x1211 = x1183 && x1208
                        val x1212 = x1185 && x1211
                        val x1214 = x1185 && x1212
                        val x1215 = x1186 && x1214
                        x1215
                    }
                )[ref=Sym(1218)],
                duplicateElimination(
                    projection(
                        selection(
                            duplicateElimination(
                                RecursionResult(
                                    unionAdd(
                                        unionAdd(
                                            projection(
                                                selection(
                                                    extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                    (x42: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                        val x43 = x42.superType
                                                        val x44 = x43.isDefined
                                                        x44
                                                    }
                                                )[ref=Sym(710)],
                                                (x699: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                    val x670 = px670 // static data: <function1>
                                                    val x700 = x699.superType
                                                    val x701 = x700.get
                                                    val x703 = x670(x699,x701)
                                                    x703
                                                }
                                            )[ref=Sym(711)],
                                            projection(
                                                unnest(
                                                    extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                    (x54: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                        val x55 = x54.interfaces
                                                        x55
                                                    }
                                                )[ref=Sym(721)],
                                                ((x712:sae.bytecode.asm.structure.ClassDeclaration,x713:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                    val x670 = px670 // static data: <function1>
                                                    val x715 = x670(x712,x713)
                                                    x715
                                                }
                                            )[ref=Sym(722)]
                                        )[ref=Sym(723)],
                                        projection(
                                            equiJoin(
                                                Recursion(
                                                    unionAdd(
                                                        projection(
                                                            selection(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x42: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                    val x43 = x42.superType
                                                                    val x44 = x43.isDefined
                                                                    x44
                                                                }
                                                            )[ref=Sym(710)],
                                                            (x699: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x700 = x699.superType
                                                                val x701 = x700.get
                                                                val x703 = x670(x699,x701)
                                                                x703
                                                            }
                                                        )[ref=Sym(711)],
                                                        projection(
                                                            unnest(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x54: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                    val x55 = x54.interfaces
                                                                    x55
                                                                }
                                                            )[ref=Sym(721)],
                                                            ((x712:sae.bytecode.asm.structure.ClassDeclaration,x713:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x715 = x670(x712,x713)
                                                                x715
                                                            }
                                                        )[ref=Sym(722)]
                                                    )[ref=Sym(723)],
                                                    Sym(743)
                                                ),
                                                Recursion(
                                                    unionAdd(
                                                        projection(
                                                            selection(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x42: sae.bytecode.asm.structure.ClassDeclaration): Boolean => {
                                                                    val x43 = x42.superType
                                                                    val x44 = x43.isDefined
                                                                    x44
                                                                }
                                                            )[ref=Sym(710)],
                                                            (x699: sae.bytecode.asm.structure.ClassDeclaration): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x700 = x699.superType
                                                                val x701 = x700.get
                                                                val x703 = x670(x699,x701)
                                                                x703
                                                            }
                                                        )[ref=Sym(711)],
                                                        projection(
                                                            unnest(
                                                                extent1452787595: Extent[sae.bytecode.asm.structure.ClassDeclaration][ref=QueryExtent(idb.SetExtent@5697c78b,true,false,false)],
                                                                (x54: sae.bytecode.asm.structure.ClassDeclaration): scala.collection.Traversable[org.objectweb.asm.Type] => {
                                                                    val x55 = x54.interfaces
                                                                    x55
                                                                }
                                                            )[ref=Sym(721)],
                                                            ((x712:sae.bytecode.asm.structure.ClassDeclaration,x713:org.objectweb.asm.Type): sae.bytecode.asm.structure.Inheritance => {
                                                                val x670 = px670 // static data: <function1>
                                                                val x715 = x670(x712,x713)
                                                                x715
                                                            }
                                                        )[ref=Sym(722)]
                                                    )[ref=Sym(723)],
                                                    Sym(743)
                                                ),
                                                Seq(
                                                    (
                                                    (x68: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                        val x70 = x68.superType
                                                        x70
                                                    },
                                                    (x29: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                                                        val x30 = x29.subType
                                                        x30
                                                    }
                                                    )
                                                )
                                            )[ref=Sym(740)],
                                            ((x724:sae.bytecode.asm.structure.TypeRelation,x725:sae.bytecode.asm.structure.TypeRelation): sae.bytecode.asm.structure.TypeRelation => {
                                                val x669 = px669 // static data: <function1>
                                                val x726 = x724.subType
                                                val x727 = x725.superType
                                                val x729 = x669(x726,x727)
                                                x729
                                            }
                                        )[ref=Sym(741)]
                                    )[ref=Sym(742)]
                                )
                            )[ref=Sym(746)],
                            (x774: sae.bytecode.asm.structure.TypeRelation with sae.bytecode.asm.structure.TypeRelation): Boolean => {
                                val x668 = px668 // static data: <function1>
                                val x751 = x668("java/lang/Cloneable")
                                val x780 = x774.superType
                                val x781 = x780 == x751
                                x781
                            }
                        )[ref=Sym(788)],
                        (x29: sae.bytecode.asm.structure.TypeRelation): org.objectweb.asm.Type => {
                            val x30 = x29.subType
                            x30
                        }
                    )[ref=Sym(799)]
                )[ref=Sym(800)],
                Seq(
                    (
                    (x123: sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration): org.objectweb.asm.Type => {
                        val x133 = x123.declaringType
                        x133
                    },
                    (x192: Any): Any => {
                        x192
                    }
                    )
                )
            )[ref=Sym(1219)],
            ((x804:sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration,x805:Any): sae.bytecode.asm.structure.MethodDeclaration with sae.bytecode.asm.structure.MethodDeclaration => {
                x804
            }
        )[ref=Sym(1220)]
    )[ref=Sym(1221)]
)[ref=Sym(1222)]

*/