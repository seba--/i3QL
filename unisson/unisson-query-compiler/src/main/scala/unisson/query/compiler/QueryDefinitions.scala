package unisson.query.compiler

import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.Database
import unisson.query.code_model._
import sae.bytecode.model.dependencies.{Dependency, inner_class, `extends`}
import sae.bytecode.model.{FieldDeclaration, MethodDeclaration}
import de.tud.cs.st.bat._
import sae._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 13:37
 *
 */
class QueryDefinitions(private val db: Database)
{
    /**
     * BEWARE INITIALIZATION ORDER OF FIELDS (the scala compiler will not warn you)
     */

    private def fromJava(unresolved: String): String = unresolved.replace('.', '/')

    private def joinByTargetElement[T <: AnyRef](view: Relation[T], viewFun: T => AnyRef,
                                                 target: Relation[SourceElement[AnyRef]]): Relation[T] =
        ((
                (
                        target,
                        (_: SourceElement[AnyRef]).element
                        ) ⋈(
                        viewFun,
                        view
                        )
                ) {(c: SourceElement[AnyRef], f: T) => f})

    def `class`(packageName: String, name: String): Relation[SourceElement[AnyRef]] =
        Π[ObjectType, SourceElement[AnyRef]] {
            new ClassDeclaration((_: ObjectType))
        }(
            σ {(o: ObjectType) => (o.packageName == fromJava(packageName) && o.simpleName == name)}(db
                    .declared_types)
        )

    def `class`(targets: Relation[SourceElement[AnyRef]]): Relation[SourceElement[AnyRef]] =
        (targets, (_: SourceElement[AnyRef]).element) ⋉(identity(_: ObjectType), db.declared_types)

    def field(declaringClasses: Relation[SourceElement[AnyRef]], name: String,
              fieldType: Relation[SourceElement[AnyRef]]) =
        Π(SourceElement(_: FieldDeclaration))(
            joinByTargetElement[FieldDeclaration](
                joinByTargetElement(
                    σ(
                        (f: FieldDeclaration) => {
                            f.name == name
                        }
                    )(db.declared_fields),
                    _.declaringClass,
                    declaringClasses),
                _.fieldType,
                fieldType
            )
        )

    def method(declaringClasses: Relation[SourceElement[AnyRef]], name: String,
               returnTypes: Relation[SourceElement[AnyRef]],
               parameterTypes: Relation[SourceElement[AnyRef]]*) =
        Π(SourceElement(_: MethodDeclaration))(
        {
            var i = -1;
            parameterTypes.foldLeft[Relation[MethodDeclaration]](
                joinByTargetElement(
                    joinByTargetElement[MethodDeclaration](
                        σ(
                            (f: MethodDeclaration) => {
                                f.name == name
                            }
                        )(db.declared_methods),
                        _.declaringRef,
                        declaringClasses),
                    _.returnType,
                    returnTypes
                )
            )(
                (view: Relation[MethodDeclaration], parameterType: Relation[SourceElement[AnyRef]]) => {
                    i = i + 1
                    val index = i
                    joinByTargetElement(
                        view,
                        m =>
                            if (index < m.parameters.length) m.parameters(index)
                            else VoidType() // we fill in void type for methods with less parameters, as this will never match
                        ,
                        parameterType
                    )
                }
            )
        }
        )

    /**
     * Direct queries for a specific type are wrapped as source code elements to allow
     * subqueries in type, e.g., all methods that return a specific a set of classes
     * @param name
     * @return
     */
    def typeQuery(name: String): Relation[SourceElement[AnyRef]] = new TypeElementView(name).asInstanceOf[Relation[SourceElement[AnyRef]]]

    /**
     * select all supertype form supertype where supertype.target exists in targets
     */
    // TODO make this special to ObjectType?
    def supertype(targets: Relation[SourceElement[AnyRef]]): Relation[SourceElement[AnyRef]] =
        Π(
            (d: Dependency[AnyRef, AnyRef]) => SourceElement(d.source)
        )(
            (
                    db.implements.∪[Dependency[AnyRef, AnyRef], `extends`](db.`extends`),
                    (_: Dependency[AnyRef, AnyRef]).target
                    ) ⋉((_: SourceElement[AnyRef]).element, targets)
        )

    def `package`(name: String): Relation[SourceElement[AnyRef]] =
        (
                Π[ObjectType, SourceElement[AnyRef]] {
                    SourceElement(_: ObjectType)
                }(σ {
                    (_: ObjectType).packageName == fromJava(name)
                }(db.declared_types))
                ) ∪
                (
                        Π[MethodDeclaration, SourceElement[AnyRef]] {
                            SourceElement[AnyRef]((_: MethodDeclaration))
                        }(σ {
                            (_: MethodDeclaration).declaringRef.packageName == fromJava(name)
                        }(db.declared_methods))
                        ) ∪
                (
                        Π[FieldDeclaration, SourceElement[AnyRef]] {
                            SourceElement[AnyRef]((_: FieldDeclaration))
                        }(σ {
                            (_: FieldDeclaration).declaringClass.packageName == fromJava(name)
                        }(db.declared_fields))
                        )

    // TODO should we compute members of classes not in the source code (these can only yield partial information
    // TODO maybe we can skip some wrapping and unwrapping of objects here, since we have TC operator the class_member type is not really used
    lazy val direct_class_members: Relation[class_member[AnyRef]] =
        Π {
            ((m: MethodDeclaration) => new class_member[AnyRef](m.declaringRef, new MethodDeclarationAdapter(m)))
        }(db.declared_methods) ∪
                Π {
                    ((f: FieldDeclaration) =>
                        new class_member[AnyRef](f
                                .declaringClass, new FieldDeclarationAdapter(f)))
                }(db.declared_fields) ∪
                Π((inner: inner_class) =>
                    new class_member[AnyRef](inner.source, new ClassDeclaration(inner
                            .target)))(db
                        .inner_classes)


    lazy val transitive_class_members: Relation[(AnyRef, AnyRef)] =
        TC(direct_class_members)((cm: class_member[AnyRef]) => (cm.source), (_: class_member[AnyRef]).target
                .element)


    def class_with_members(packageName: String, className: String): Relation[SourceElement[AnyRef]] =
        class_with_members(packageName + "." + className)

    def class_with_members(qualifiedClass: String): Relation[SourceElement[AnyRef]] =
        Π {
            SourceElement[AnyRef]((_: ObjectType))
        }(
            σ {
                (_: ObjectType) == ObjectType(fromJava(qualifiedClass))
            }(db.declared_types)
        ) ∪
                Π {(cm: (AnyRef, AnyRef)) => SourceElement[AnyRef](cm._2)}(
                    σ {
                        (_: (AnyRef, AnyRef))._1 == ObjectType(fromJava(qualifiedClass))
                    }(transitive_class_members)
                )


    // reuse this query so the supertype is not recomputed multiple times
    lazy val supertypeTrans = TC(db.implements.∪[Dependency[AnyRef, AnyRef], `extends`](db.`extends`))(_.source, _
            .target)

    def transitive_supertype(targets: Relation[SourceElement[AnyRef]]): Relation[SourceElement[AnyRef]] =
        δ(// TODO something is not right here, this should not require a delta, values should be distinct on their own
            Π(
                (d: (AnyRef, AnyRef)) => SourceElement(d._1) // _.source
            )(
                (
                        supertypeTrans,
                        (_: (AnyRef, AnyRef))._2 // _.target
                        ) ⋉((_: SourceElement[AnyRef]).element, targets)
            )
        )

    /**
     * rewrites the query so it will be used transitively
     * // TODO rewriting is currently very unsatisfying, there are multiple proxy objects in the tree
     */
    def transitive(target: Relation[SourceElement[AnyRef]]): Relation[SourceElement[AnyRef]] = {

        target match {
            case p@Π(
            func: (Dependency[AnyRef, AnyRef] => SourceElement[AnyRef]),
            sj@sae.syntax.RelationalAlgebraSyntax.⋉(
            transitiveQuery: Relation[Dependency[AnyRef, AnyRef]],
            transitiveKey: (Dependency[AnyRef, AnyRef] => AnyRef),
            outerQuery: Relation[SourceElement[AnyRef]],
            outerKey: (SourceElement[AnyRef] => AnyRef)
            )
            ) => {
                //p.relation.removeObserver(p.asInstanceOf[Observer[Dependency[AnyRef, AnyRef]]])
                sj.leftIndex.relation.removeObserver(sj.leftIndex)
                sj.rightIndex.relation.removeObserver(sj.rightIndex)

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
                    )(TC(transitiveQuery)(_.source, _.target))

                Π(func)((transitiveDependencies, transitiveKey) ⋉(outerKey, outerQuery))
            }
        }
    }

    /**
     * select all (class, member) from transitive_class_members where class exists in target
     */
    def class_with_members(target: Relation[SourceElement[AnyRef]]): Relation[SourceElement[AnyRef]] =
        Π(identity(_: SourceElement[AnyRef]))(
            σ((_: SourceElement[AnyRef]) match {
                case SourceElement(_: ObjectType) => true
                case _ => false
            }
            )(target)
        ) ∪
                Π {(tuple: (AnyRef, AnyRef)) => SourceElement[AnyRef](tuple._2)}(
                    (transitive_class_members, (cm: (AnyRef, AnyRef)) =>
                        SourceElement[AnyRef](cm
                                ._1)) ⋉(identity[SourceElement[AnyRef]], target)
                )


    implicit def viewToUnissonConcatenator[Domain <: AnyRef](relation: Relation[Domain]): UnissionInfixConcatenator[Domain] =
        UnissionInfixConcatenator(relation)

    case class UnissionInfixConcatenator[Domain <: AnyRef](left: Relation[Domain])
    {
        def or[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass](otherRelation: Relation[OtherDomain]): Relation[CommonSuperClass] = left ∪ otherRelation

        def without(otherRelation: Relation[Domain]): Relation[Domain] = left ∖ otherRelation

    }

}