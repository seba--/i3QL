package unisson

import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Field, Method}
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{inner_class, Dependency, `extends`}
import sae.{Observer, LazyView}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 13:37
 *
 */
class Queries( val db : BytecodeDatabase )
{

    /**
     * select all supertype form supertype where supertype.target exists in targets
     */
    def supertype( targets : LazyView[SourceElement[AnyRef]]) : LazyView[SourceElement[AnyRef]] =
        Π(
            (d:Dependency[AnyRef, AnyRef]) => new SourceElement(d.source)
         )(
            (
                    db.implements.∪[Dependency[AnyRef, AnyRef], `extends`](db.`extends`),
                    (_:Dependency[AnyRef, AnyRef]).target
            ) ⋉ ((_:SourceElement[AnyRef]).element, targets)
        )
    /*
        Π[`extends`, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:`extends`).source) }(
            (db.`extends`, target _) ⊳ ((_:SourceElement[AnyRef]).element, target)
        )
    */

    def `class`(packageName : String, name : String) : LazyView[SourceElement[AnyRef]] =
Π[ObjectType, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:ObjectType)) }(σ{ (o:ObjectType) => (o.packageName == fromJava(packageName) && o.simpleName == fromJava(name) )}(db.classfiles))

    def `package`(name : String) : LazyView[SourceElement[AnyRef]] =
        (
             Π[ObjectType, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:ObjectType)) }(σ{ (_:ObjectType).packageName == fromJava(name)}(db.classfiles))
        ) ∪
        (
            Π[Method, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:Method)) } (σ{ (_:Method).declaringRef.packageName == fromJava(name) }(db.classfile_methods))
        ) ∪
        (
            Π[Field, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:Field)) } (σ{ (_:Field).declaringClass.packageName == fromJava(name) }(db.classfile_fields))
        )

    /**
     * BEWARE INITIALIZATION ORDER OF FIELDS (scala compiler will not warn you)
     */

    // TODO should we compute members of classes not in the source code (these can only yield partial information
    // TODO maybe we can skip some wrapping and unwrapping of objects here, since we have TC operator the class_member type is not really used
    val direct_class_members : LazyView[class_member[AnyRef]] =
        Π{ ( (m : Method) =>  new class_member[AnyRef](m.declaringRef, new SourceElement[AnyRef]( m ) )) }(db.classfile_methods) ∪
        Π{ ( (f : Field) =>  new class_member[AnyRef](f.declaringClass, new SourceElement[AnyRef]( f ) )) }(db.classfile_fields) ∪
        Π( (inner:inner_class) => new class_member[AnyRef](inner.source, new SourceElement[AnyRef]( inner.target ) ) ) (db.inner_classes)


     val transitive_class_members : LazyView[(AnyRef, AnyRef)] =
        TC(direct_class_members)( (cm:class_member[AnyRef]) => (cm.source), (_:class_member[AnyRef]).target.element )


    def class_with_members(packageName : String, className : String) : LazyView[SourceElement[AnyRef]] = class_with_members(packageName + "." + className)

    def class_with_members(qualifiedClass : String) : LazyView[SourceElement[AnyRef]] =
        Π{ new SourceElement[AnyRef]( (_:ObjectType) ) } (
            σ{ (_:ObjectType) == ObjectType(fromJava(qualifiedClass))}(db.classfiles)
        ) ∪
        Π{ (cm:(AnyRef, AnyRef)) => new SourceElement[AnyRef](cm._2) } (
            σ{ (_:(AnyRef, AnyRef))._1 == ObjectType(fromJava(qualifiedClass))} (transitive_class_members)
        )


    /**
     * rewrites the query so it will be used transitively
     */
    def transitive(  target : LazyView[SourceElement[AnyRef]] ) : LazyView[SourceElement[AnyRef]] =

            target match
            {
                case p @ Π(
                            func:(Dependency[AnyRef, AnyRef] => SourceElement[AnyRef]),
                            oldQuery:LazyView[Dependency[AnyRef, AnyRef]]
                        ) =>
                    {
                        p.relation.removeObserver(p.asInstanceOf[Observer[Dependency[AnyRef, AnyRef]]])
                        Π[(AnyRef, AnyRef),SourceElement[AnyRef]](
                            // wrap in a dependency again so we can reuse the projection function
                            (t:(AnyRef,AnyRef)) => func.asInstanceOf[Dependency[AnyRef, AnyRef] => SourceElement[AnyRef]](
                                new Dependency[AnyRef,AnyRef] {
                                    val source = t._1
                                    val target = t._2
                                }
                            )

                        )(TC(oldQuery)(_.source, _.target))
                    }
            }

    /**
     * select all (class, member) from transitive_class_members where class exists in target
     */
    def class_with_members(target : LazyView[SourceElement[AnyRef]]) : LazyView[SourceElement[AnyRef]] = Π{ (tuple:(AnyRef, AnyRef)) => new SourceElement[AnyRef]( tuple._2 ) } (
            (transitive_class_members, (cm:(AnyRef, AnyRef)) => new SourceElement[AnyRef](cm._1)) ⋉ (identity[SourceElement[AnyRef]], target)
        )


    private def fromJava(unresolved : String) : String = unresolved.replace('.', '/')

    implicit def viewToUnissonConcatenator[Domain <: AnyRef](relation: LazyView[Domain]): UnissionInfixConcatenator[Domain] =
        UnissionInfixConcatenator(relation)

    case class UnissionInfixConcatenator[Domain <: AnyRef](left: LazyView[Domain])
    {
        def or[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass](otherRelation: LazyView[OtherDomain]): LazyView[CommonSuperClass] = left ∪ otherRelation

        def without(otherRelation: LazyView[Domain]): LazyView[Domain] = left ∖ otherRelation

    }
}


object Queries
{
    def source(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.source.asInstanceOf[AnyRef])

    def target(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.target.asInstanceOf[AnyRef])

}