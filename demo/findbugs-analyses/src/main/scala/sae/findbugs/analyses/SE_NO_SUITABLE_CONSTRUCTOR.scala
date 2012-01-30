package sae.findbugs.analyses

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.Database
import sae.LazyView
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.Method
import sae.bytecode.model.dependencies.{`extends`, implements}

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 19:29
 *
 * FINDBUGS: Se: Class is Serializable but its superclass doesn't define a default constructor (SE_NO_SUITABLE_CONSTRUCTOR)
 * Returns a view of tuples in the form extends(serializableClass, superClassWithoutDefaultConstructor)
 */
object SE_NO_SUITABLE_CONSTRUCTOR
{

    val serializable = ObjectType("java/io/Serializable")

    def apply(database: Database): LazyView[`extends`] = {
        // TODO maybe use distinct here
        val serializableClasses = Π(
            (_: implements).source
        )(
            σ(
                (_: implements).target == serializable
            )(
                database.implements
            )
        )

        val superClassRelationsOfSerializableClasses = (
                (
                        serializableClasses,
                        identity(_: ObjectType)
                        ) ⋈(
                        (_: `extends`).source,
                        database.`extends`
                        )
                ) {(serializableClass: ObjectType,
                    supertypeRelation: `extends`) =>
            supertypeRelation
        }

        val noargConstructors = σ( (m: Method) =>
        {
            m.isConstructor && m.parameters.isEmpty
        }) (database.classfile_methods)

        val noargConstructorsInSuperClassOfSerializableClasses = (
                (
                        noargConstructors,
                        (_: Method).declaringRef
                        ) ⋈(
                        (_: `extends`).target,
                        superClassRelationsOfSerializableClasses
                        )
                ) {
            (m: Method, superTypeRelation: `extends`) => (m, superTypeRelation)
        }

        val serializableClassWithoutDefaultConstructor = (
                (
                        superClassRelationsOfSerializableClasses,
                        identity(_: `extends`)
                        ) ⊳(
                        (_: (Method, `extends`))._2,
                        noargConstructorsInSuperClassOfSerializableClasses
                        )
                )

        serializableClassWithoutDefaultConstructor
    }

}