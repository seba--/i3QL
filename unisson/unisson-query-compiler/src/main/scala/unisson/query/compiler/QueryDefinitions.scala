package unisson.query.compiler

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Field, Method}
import sae.syntax.RelationalAlgebraSyntax._
import sae.LazyView
import sae.bytecode.Database
import unisson.query.code_model._
import sae.bytecode.model.dependencies.{Dependency, inner_class, `extends`}

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

    def `class`(packageName: String, name: String): LazyView[SourceElement[AnyRef]] =
        Π[ObjectType, SourceElement[AnyRef]] {
            new ClassDeclaration((_: ObjectType))
        }(
            σ {(o: ObjectType) => (o.packageName == fromJava(packageName) && o.simpleName == name)}(db
                    .classfiles)
        )

    def `class`(targets: LazyView[SourceElement[AnyRef]]): LazyView[SourceElement[AnyRef]] =
        (targets, (_: SourceElement[AnyRef]).element) ⋉(identity(_: ObjectType), db.classfiles)

    /**
     * select all supertype form supertype where supertype.target exists in targets
     */

    // TODO make this special to ObjectType?
    def supertype(targets: LazyView[SourceElement[AnyRef]]): LazyView[SourceElement[AnyRef]] =
        Π(
            (d: Dependency[AnyRef, AnyRef]) => SourceElement(d.source)
        )(
            (
                    db.implements.∪[Dependency[AnyRef, AnyRef], `extends`](db.`extends`),
                    (_: Dependency[AnyRef, AnyRef]).target
                    ) ⋉((_: SourceElement[AnyRef]).element, targets)
        )

    def `package`(name: String): LazyView[SourceElement[AnyRef]] =
        (
                Π[ObjectType, SourceElement[AnyRef]] {
                    SourceElement(_: ObjectType)
                }(σ {
                    (_: ObjectType).packageName == fromJava(name)
                }(db.classfiles))
                ) ∪
                (
                        Π[Method, SourceElement[AnyRef]] {
                            SourceElement[AnyRef]((_: Method))
                        }(σ {
                            (_: Method).declaringRef.packageName == fromJava(name)
                        }(db.classfile_methods))
                        ) ∪
                (
                        Π[Field, SourceElement[AnyRef]] {
                            SourceElement[AnyRef]((_: Field))
                        }(σ {
                            (_: Field).declaringClass.packageName == fromJava(name)
                        }(db.classfile_fields))
                        )

    // TODO should we compute members of classes not in the source code (these can only yield partial information
    // TODO maybe we can skip some wrapping and unwrapping of objects here, since we have TC operator the class_member type is not really used
    lazy val direct_class_members: LazyView[class_member[AnyRef]] =
        Π {
            ((m: Method) => new class_member[AnyRef](m.declaringRef, new MethodDeclaration(m)))
        }(db.classfile_methods) ∪
                Π {
                    ((f: Field) => new class_member[AnyRef](f.declaringClass, new FieldDeclaration(f)))
                }(db.classfile_fields) ∪
                Π((inner: inner_class) => new class_member[AnyRef](inner.source, new ClassDeclaration(inner.target)))(db
                        .inner_classes)


    lazy val transitive_class_members: LazyView[(AnyRef, AnyRef)] =
        TC(direct_class_members)((cm: class_member[AnyRef]) => (cm.source), (_: class_member[AnyRef]).target.element)


    def class_with_members(packageName: String, className: String): LazyView[SourceElement[AnyRef]] =
        class_with_members(packageName + "." + className)

    def class_with_members(qualifiedClass: String): LazyView[SourceElement[AnyRef]] =
        Π {
            SourceElement[AnyRef]((_: ObjectType))
        }(
            σ {
                (_: ObjectType) == ObjectType(fromJava(qualifiedClass))
            }(db.classfiles)
        ) ∪
                Π {(cm: (AnyRef, AnyRef)) => SourceElement[AnyRef](cm._2)}(
                    σ {
                        (_: (AnyRef, AnyRef))._1 == ObjectType(fromJava(qualifiedClass))
                    }(transitive_class_members)
                )


    // reuse this query so the supertype is not recomputed multiple times
    lazy val supertypeTrans = TC(db.implements.∪[Dependency[AnyRef, AnyRef], `extends`](db.`extends`))(_.source, _
            .target)

    def transitive_supertype(targets: LazyView[SourceElement[AnyRef]]): LazyView[SourceElement[AnyRef]] =
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
    def transitive(target: LazyView[SourceElement[AnyRef]]): LazyView[SourceElement[AnyRef]] = {

        target match {
            case p@Π(
            func: (Dependency[AnyRef, AnyRef] => SourceElement[AnyRef]),
            sj @sae.syntax.RelationalAlgebraSyntax.⋉ (
                    transitiveQuery: LazyView[Dependency[AnyRef, AnyRef]],
                    transitiveKey: (Dependency[AnyRef, AnyRef] => AnyRef),
                    outerQuery: LazyView[SourceElement[AnyRef]],
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
    def class_with_members(target: LazyView[SourceElement[AnyRef]]): LazyView[SourceElement[AnyRef]] =
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


    implicit def viewToUnissonConcatenator[Domain <: AnyRef](relation: LazyView[Domain]): UnissionInfixConcatenator[Domain] =
        UnissionInfixConcatenator(relation)

    case class UnissionInfixConcatenator[Domain <: AnyRef](left: LazyView[Domain])
    {
        def or[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass](otherRelation: LazyView[OtherDomain]): LazyView[CommonSuperClass] = left ∪ otherRelation

        def without(otherRelation: LazyView[Domain]): LazyView[Domain] = left ∖ otherRelation

    }

}