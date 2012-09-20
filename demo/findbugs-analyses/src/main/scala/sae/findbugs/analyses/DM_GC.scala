package sae.findbugs.analyses

import sae.Relation
import sae.bytecode.model.dependencies.{Dependency, invoke_virtual, invoke_static}
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.{BytecodeDatabase, Database}
import sae.bytecode.model.instructions.{invokestatic, invokevirtual}
import sae.bytecode.model.{Instr, MethodDeclaration, MethodReference}
import de.tud.cs.st.bat.{VoidType, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 11:16
 *
 *
 * FINDBUGS: Dm: Explicit garbage collection; extremely dubious except in benchmarking code (DM_GC)
 */
object DM_GC
{

    private val systemGC = MethodReference(ObjectType("java/lang/System"), "gc", Seq(), VoidType())

    private val runtimeGC = MethodReference(ObjectType("java/lang/Runtime"), "gc", Seq(), VoidType())


    def apply(database: BytecodeDatabase): Relation[Dependency[MethodDeclaration, MethodReference]] = {
        Π(
            (_: Instr[_]) match {
                case invokestatic(declaringMethod, _, callee) => new Dependency[MethodDeclaration, MethodReference] {
                    def source = declaringMethod
                    def target = callee
                }
                case invokevirtual(declaringMethod, _, callee) => new Dependency[MethodDeclaration, MethodReference] {
                    def source = declaringMethod
                    def target = callee
                }
            }
        )(
            σ(  (_: Instr[_]) match {
                case invokestatic(_, _, MethodReference(ObjectType("java/lang/System"), "gc", Seq(), r)) if r.isVoidType => true
                case invokevirtual(_, _, MethodReference(ObjectType("java/lang/Runtime"), "gc", Seq(), r)) if r.isVoidType => true
                case _ => false
            }
            )
            (database.instructions)
        )
    }

    /** not using the already precomputed relations in the database **/
    def unoptimized(database: BytecodeDatabase): Relation[Dependency[MethodDeclaration, MethodReference]] = {
        val invokestatic = Π( (_:Instr[_]).asInstanceOf[invokestatic] )(σ[invokestatic](database.instructions))

        val invokevirtual = Π( (_:Instr[_]).asInstanceOf[invokevirtual] )(σ[invokevirtual](database.instructions))

        (
                Π(
                    (i: invokestatic) => invoke_static(i.declaringMethod, i.method))(
                    σ((_: invokestatic).method == systemGC)(invokestatic)
                )
                ).∪[Dependency[MethodDeclaration, MethodReference], invoke_virtual](
            Π(
                (i: invokevirtual) => invoke_virtual(i.declaringMethod, i.method)
            )(
                σ((_: invokevirtual)
                        .method == runtimeGC)(invokevirtual)
            )
        )
    }

    def optimized(database: BytecodeDatabase): Relation[Dependency[MethodDeclaration, MethodReference]] =
        σ((_: invoke_static).target == systemGC)(database.invoke_static)
                .∪[Dependency[MethodDeclaration, MethodReference], invoke_virtual](
            σ((_: invoke_virtual).target == runtimeGC)(database.invoke_virtual)
        )

}