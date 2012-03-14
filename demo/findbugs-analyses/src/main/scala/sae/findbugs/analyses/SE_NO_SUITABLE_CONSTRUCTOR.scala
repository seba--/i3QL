package sae.findbugs.analyses

import sae.bytecode.Database
import sae.LazyView
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{`extends`, implements}
import de.tud.cs.st.bat.{ReferenceType, ObjectType}
import sae.bytecode.model.{MethodDeclaration, MethodReference}

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

    def apply(database: Database): LazyView[ObjectType] = {

        val serializableClasses = Π(
            (_: implements).source
        )(
            σ(
                (_: implements).target == serializable
            )(
                database.implements
            )
        )

        // super types may appear more than once for different serializable classes
        val superTypesOfSerializableClasses = δ((
                (
                        serializableClasses,
                        identity(_: ObjectType)
                        ) ⋈(
                        (_: `extends`).source,
                        database.`extends`
                        )
                ) {
            (serializableClass: ObjectType, supertypeRelation: `extends`) => supertypeRelation.target
        })

        // we wish to select only classes that are part of the analyzed code base
        val analyzedSuperClassesOfSerializableClasses = (
                (
                        superTypesOfSerializableClasses,
                        identity(_: ObjectType)
                        ) ⋈(
                        identity(_: ObjectType),
                        database.declared_types
                        )
                ) {
            (superType: ObjectType, classFileType: ObjectType) => superType
        }

        val noargConstructors = σ( (m: MethodDeclaration) =>
        {
            m.isConstructor && m.parameters.isEmpty
        }) (database.declared_methods)

        val noargConstructorsInSuperClassOfSerializableClasses = (
                (
                        noargConstructors,
                        (_: MethodDeclaration).declaringRef
                        ) ⋈(
                        identity(_:ObjectType),
                        analyzedSuperClassesOfSerializableClasses
                        )
                ) {
            (m: MethodDeclaration, superType: ObjectType) => m
        }

        val serializableClassWithoutDefaultConstructor = (
                (
                        analyzedSuperClassesOfSerializableClasses,
                        identity(_: ReferenceType)
                        ) ⊳(
                        (_: (MethodDeclaration)).declaringRef,
                        noargConstructorsInSuperClassOfSerializableClasses
                        )
                )

        serializableClassWithoutDefaultConstructor
    }

}