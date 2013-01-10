package unisson.query.compiler

import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.BytecodeDatabase
import unisson.query.code_model._
import sae.Relation
import de.tud.cs.st.bat.resolved.ObjectType
import sae.bytecode.structure.{InnerClass, InheritanceRelation, MethodDeclaration, FieldDeclaration}
import de.tud.cs.st.vespucci.interfaces.{IClassDeclaration, ICodeElement}
import sae.operators.impl.ExistsInSameDomainView
import sae.syntax.sql._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 13:37
 *
 */
class QueryDefinitions(private val db: BytecodeDatabase)
{
    /**
     * BEWARE INITIALIZATION ORDER OF FIELDS (the scala compiler will not warn you)
     */

    
    
    private def fromJava(unresolved: String): String = unresolved.replace ('.', '/')

    def `class`(packageName: String, name: String): Relation[ICodeElement] =
        Π[ObjectType, ICodeElement] {
            SourceElementFactory ((_: ObjectType))
        }(
            σ {
                (o: ObjectType) => (o.packageName == fromJava (packageName) && o.simpleName == name)
            }(db.typeDeclarations.asMaterialized)
        )

    def `class`(targets: Relation[ICodeElement]): Relation[ICodeElement] =
        new ExistsInSameDomainView[ICodeElement](targets.asMaterialized, db.typeDeclarations.asMaterialized.asInstanceOf[Relation[ICodeElement]].asMaterialized)

    //(targets, identity(_: ICodeElement)) ⋉ (identity (_: ICodeElement), db.typeDeclarations.asMaterialized)


    def direct_field(packageName: String, simpleName: String, name: String, fieldType: String): Relation[ICodeElement] = compile (
        SELECT ((f: FieldDeclaration) => SourceElementFactory (f)) FROM db.fieldDeclarations.asMaterialized WHERE
            (_.declaringType.packageName == packageName) AND
            (_.declaringType.simpleName == simpleName) AND
            (_.name == name) AND
            (_.fieldType.toJava == fieldType)
    )

    def method(declaringClasses: Relation[ICodeElement], name: String,
               returnTypes: Relation[ICodeElement],
               parameterTypes: Relation[ICodeElement]*) =
        throw new UnsupportedOperationException ("method query with subqueries not supported")

    def field(declaringClasses: Relation[ICodeElement],
              name: String,
              fieldType: Relation[ICodeElement]) =
        throw new UnsupportedOperationException ("field query with subqueries not supported")

    /*
        private def joinByTarget[T <: AnyRef](view: Relation[T], viewFun: T => AnyRef,
                                              target: Relation[ICodeElement]): Relation[T] =
            ((
                (
                    target,
                    (_: ICodeElement)
                    ) ⋈ (
                    viewFun,
                    view
                    )
                )
            {
                (c: ICodeElement, f: T) => f
            })

        def field(declaringClasses: Relation[ICodeElement],
                  name: String,
                  fieldType: Relation[ICodeElement]) =
            Π (SourceElementFactory (_: FieldDeclaration))(
                joinByTarget[FieldDeclaration](
                    joinByTarget (
                        σ (
                            (f: FieldDeclaration) => {
                                f.name == name
                            }
                        )(db.fieldDeclarations.asMaterialized),
                        _.declaringType,
                        declaringClasses),
                    _.fieldType,
                    fieldType
                )
            )

        def method(declaringClasses: Relation[ICodeElement], name: String,
                   returnTypes: Relation[ICodeElement],
                   parameterTypes: Relation[ICodeElement]*) =
            Π (SourceElementFactory (_: MethodDeclaration))(
            {
                var i = -1
                parameterTypes.foldLeft[Relation[MethodDeclaration]](
                    joinByTarget (
                        joinByTarget[MethodDeclaration](
                            σ (
                                (f: MethodDeclaration) => {
                                    f.name == name
                                }
                            )(db.methodDeclarations.asMaterialized),
                            _.declaringClassType,
                            declaringClasses),
                        _.returnType,
                        returnTypes
                    )
                )(
                    (view: Relation[MethodDeclaration], parameterType: Relation[ICodeElement]) => {
                        i = i + 1
                        val index = i
                        joinByTarget (
                            view,
                            m =>
                                if (index < m.parameterTypes.length) m.parameterTypes (index)
                                else VoidType // we fill in void type for methods with less parameters, as this will never match
                            ,
                            parameterType
                        )
                    }
                )
            }
            )
    */

    /**
     * Direct queries for a specific type are wrapped as source code elements to allow
     * subqueries in type, e.g., all methods that return a specific a set of classes
     * @param name
     * @return
     */
    def typeQuery(name: String): Relation[ICodeElement] =
        new TypeElementView (name).asInstanceOf[Relation[ICodeElement]]


    val superTypeElement: InheritanceRelation => ICodeElement = (i: InheritanceRelation) =>
        SourceElementFactory (i.superType)

    /**
     * select all supertype form supertype where supertype.target exists in targets
     */
    def supertype(targets: Relation[ICodeElement]): Relation[ICodeElement] =
        Π (
            (d: InheritanceRelation) => SourceElementFactory (d.subType)
        )(
            (
                db.inheritance,
                superTypeElement
                ) ⋉ (
                identity[ICodeElement],
                targets
                )
        )

    def `package`(name: String): Relation[ICodeElement] =
        (
            Π[ObjectType, ICodeElement] {
                SourceElementFactory (_: ObjectType)
            }(σ {
                (_: ObjectType).packageName == fromJava (name)
            }(db.typeDeclarations.asMaterialized))
            ) ⊎
            (
                Π[MethodDeclaration, ICodeElement] {
                    SourceElementFactory ((_: MethodDeclaration))
                }(σ {
                    (_: MethodDeclaration).declaringClassType.packageName == fromJava (name)
                }(db.methodDeclarations.asMaterialized))
                ) ⊎
            (
                Π[FieldDeclaration, ICodeElement] {
                    SourceElementFactory ((_: FieldDeclaration))
                }(σ {
                    (_: FieldDeclaration).declaringType.packageName == fromJava (name)
                }(db.fieldDeclarations.asMaterialized))
                )


    def isInnerClass(o: ObjectType) = o.className.indexOf ('$') > 0

    lazy val direct_inner_class_members: Relation[ClassMember] = compile (
        SELECT ((m: MethodDeclaration) => new ClassMember (m.declaringClassType, SourceElementFactory (m))) FROM
            db.methodDeclarations.asMaterialized WHERE
            (m => isInnerClass (m.declaringClassType)) UNION_ALL (
            SELECT ((f: FieldDeclaration) => new ClassMember (f.declaringClassType, SourceElementFactory (f))) FROM
                db.fieldDeclarations.asMaterialized WHERE
                (f => isInnerClass (f.declaringClassType))
            ) UNION_ALL (
            SELECT ((inner: InnerClass) => new ClassMember (inner.outerType, SourceElementFactory (inner.classType))) FROM db.innerClasses
            )
    )


    lazy val transitive_inner_class_members: Relation[(ICodeElement, ICodeElement)] =
        TC (direct_inner_class_members)(
            (_: ClassMember).outerType,
            (_: ClassMember).member
        )

    def class_with_members(packageName: String, className: String): Relation[ICodeElement] =
        class_with_members (packageName + "." + className)

    def typeAsCodeElement: ObjectType => ICodeElement = (o: ObjectType) => o

    def methodAsCodeElement: MethodDeclaration => ICodeElement = (o: MethodDeclaration) => o

    def fieldAsCodeElement: FieldDeclaration => ICodeElement = (o: FieldDeclaration) => o

    def class_with_members(qualifiedClass: String): Relation[ICodeElement] = {
        val className = fromJava (qualifiedClass)
        compile (
            SELECT (typeAsCodeElement) FROM db.typeDeclarations.asMaterialized WHERE
                (_.className.startsWith (className)) UNION_ALL (
                SELECT (methodAsCodeElement) FROM db.methodDeclarations.asMaterialized WHERE
                    (_.declaringClassType.className.startsWith (className))
                ) UNION_ALL (
                SELECT (fieldAsCodeElement) FROM db.fieldDeclarations.asMaterialized WHERE
                    (_.declaringClassType.className.startsWith (className))
                ) //UNION_ALL (
            //SELECT ((_: (ICodeElement, ICodeElement))._2) FROM transitive_inner_class_members WHERE
            //  ((_: (ICodeElement, ICodeElement))._1 == ObjectType (fromJava (qualifiedClass)))
            //)
        )
    }

    def transitive_supertype(targets: Relation[ICodeElement]): Relation[ICodeElement] =
        δ (// TODO something is not right here, this should not require a delta, values should be distinct on their own
            Π (
                (d: InheritanceRelation) => SourceElementFactory (d.subType) // _.source
            )(
                (
                    db.subTypes,
                    (_: InheritanceRelation).superType.asInstanceOf[ICodeElement] // _.target
                    ) ⋉ (
                    identity[ICodeElement],
                    targets
                    )
            )
        )

    /**
     * rewrites the query so it will be used transitively
     * // TODO rewriting is currently very unsatisfying, there are multiple proxy objects in the tree
     */
    /*
    def transitive(target: Relation[ICodeElement]): Relation[ICodeElement] = {

        target match {
            case p@Π (
            func: (Dependency[AnyRef, AnyRef] => ICodeElement),
            sj@sae.syntax.RelationalAlgebraSyntax.⋉ (
            transitiveQuery: Relation[Dependency[AnyRef, AnyRef]],
            transitiveKey: (Dependency[AnyRef, AnyRef] => AnyRef),
            outerQuery: Relation[ICodeElement],
            outerKey: (ICodeElement => AnyRef)
            )
            ) =>
            {
                //p.relation.removeObserver(p.asInstanceOf[Observer[Dependency[AnyRef, AnyRef]]])
                sj.leftIndex.relation.removeObserver (sj.leftIndex)
                sj.rightIndex.relation.removeObserver (sj.rightIndex)

                val transitiveDependencies =
                    Π[(AnyRef, AnyRef), Dependency[AnyRef, AnyRef]](
                        // wrap in a dependency again so we can reuse the old key and projection functions
                        (t: (AnyRef, AnyRef)) =>
                            (
                                new Dependency[AnyRef, AnyRef]
                                {
                                    val source = t._1
                                    val target = t._2
                                }
                                )
                    )(TC (transitiveQuery)(_.source, _.target))

                Π (func)((transitiveDependencies, transitiveKey) ⋉ (outerKey, outerQuery))
            }
        }
    }
    */

    /**
     * select all (class, member) from transitive_class_members where class exists in target
     */
    def class_with_members(target: Relation[ICodeElement]): Relation[ICodeElement] =
        Π (identity (_: ICodeElement))(
            σ ((_: ICodeElement).isInstanceOf[IClassDeclaration])(target)
        ) ⊎
            Π {
                (_: (ICodeElement, ICodeElement))._2
            }(
                (transitive_inner_class_members, (cm: (ICodeElement, ICodeElement)) =>
                    cm._1) ⋉ (identity[ICodeElement], target)
            )


    implicit def viewToUnissonConcatenator[Domain <: AnyRef](relation: Relation[Domain]): UnissionInfixConcatenator[Domain] =
        UnissionInfixConcatenator (relation)

    case class UnissionInfixConcatenator[Domain <: AnyRef](left: Relation[Domain])
    {
        def or[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass](otherRelation: Relation[OtherDomain]): Relation[CommonSuperClass] = left ⊎ otherRelation

        def without(otherRelation: Relation[Domain]): Relation[Domain] = left ∖ otherRelation

    }

}