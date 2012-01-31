package sae.findbugs.analyses

import sae.LazyView
import sae.bytecode.model.MethodReference
import de.tud.cs.st.bat.{VoidType, ObjectType}
import sae.bytecode.model.dependencies.{Dependency, invoke_virtual, invoke_static}
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.Database

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

    def apply(database: Database): LazyView[Dependency[MethodReference, MethodReference]] =
        σ((_: invoke_static).target == systemGC)(database.invoke_static).∪[Dependency[MethodReference, MethodReference], invoke_virtual](
            σ((_: invoke_virtual).target == runtimeGC)(database.invoke_virtual)
        )

}