package sae.analyses.findbugs.selected.oo.optimized

import sae.bytecode._
import instructions.InvokeInstruction
import sae.Relation
import sae.bytecode.structure.MethodDeclaration
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.{ReferenceType, ObjectType}
import sae.analyses.findbugs.base.oo.Definitions
import sae.operators.impl.{TransactionalDifferenceView, EquiJoinView, DifferenceView, TransactionalEquiJoinView}

/**
 *
 * @author Ralf Mitschke
 *
 *         TODO consider optimization together with CN_IDIOM
 *         TODO consider optimization together with CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
 *
 */
object CN_IDIOM_NO_SUPER_CALL
    extends (BytecodeDatabase => Relation[MethodDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._
        val definitions = Definitions(database)
        import definitions._
        val filtered = compile(
            SELECT(*) FROM implementersOfClone WHERE
                    (!_.declaringClass.isInterface) AND
                    (!_.declaringClass.isAnnotation) AND
                    (!_.isAbstract) AND
                    (_.declaringClass.superClass.isDefined)
        )

        val invokes = compile(
            SELECT DISTINCT ((i: InvokeInstruction) => (i.declaringMethod, i.receiverType)) FROM invokeSpecial WHERE
                    (_.name == "clone") AND
                    (_.parameterTypes == Nil) AND
                    (_.returnType == ObjectType.Object)
        )

        val join = new TransactionalEquiJoinView(filtered,
            invokes,
            (m: MethodDeclaration) => (m, m.declaringClass.superClass.get),
            identity[(MethodDeclaration,ReferenceType)] _,
            (m: MethodDeclaration, e: (MethodDeclaration,ReferenceType)) => m
        )

        // TODo make this also local
        new TransactionalDifferenceView[MethodDeclaration](filtered, join)
    }

}