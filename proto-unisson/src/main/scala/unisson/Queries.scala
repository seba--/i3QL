package unisson

import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Field, Method}
import sae.syntax.RelationalAlgebraSyntax._
import sae.LazyView
import sae.bytecode.model.dependencies.{inner_class, Dependency}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 13:37
 *
 */
class Queries( val db : BytecodeDatabase )
{
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

    /*
    def class_with_members_old(qualifiedClass : String) : LazyView[SourceElement[AnyRef]] =
        (
             Π[ObjectType, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:ObjectType)) }(σ{ (_:ObjectType) == ObjectType(fromJava(qualifiedClass))}(db.classfiles))
        ) ∪ (
            Π[Method, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:Method)) } (σ{ (_:Method).declaringRef == ObjectType(fromJava(qualifiedClass)) }(db.classfile_methods))
        ) ∪ (
            Π[Field, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:Field)) } (σ{ (_:Field).declaringClass == ObjectType(fromJava(qualifiedClass)) }(db.classfile_fields))
        ) ∪ (
            // TODO select inner classes transitively
            // select all inner classes as members. anonymous inner classes can not be depended upon, but may declare dependencies
            Π[inner_class, SourceElement[AnyRef]]( (in:inner_class) => SourceElement[AnyRef](in.target) ) (σ{(_:inner_class).source == ObjectType(fromJava(qualifiedClass))}(db.inner_classes))
        )
    */

    def class_with_members(qualifiedClass : String) : LazyView[SourceElement[AnyRef]] =
        Π{ new SourceElement[AnyRef]( (_:ObjectType) ) } (
            σ{ (_:ObjectType) == ObjectType(fromJava(qualifiedClass))}(db.classfiles)
        ) ∪
        Π{ (cm:class_member[AnyRef]) => cm.target } (
            σ{ (_:class_member[AnyRef]).source == ObjectType(fromJava(qualifiedClass))} (class_members)
        )

    /**
     * BEWARE INITIALIZATION ORDER OF FIELDS (scala compiler will not warn you)
     */

    // TODO should we compute members of classes not in the source code (these can only yield partial information
    val direct_class_members : LazyView[class_member[AnyRef]] =
        Π{ ( (m : Method) =>  new class_member[AnyRef](m.declaringRef, new SourceElement[AnyRef]( m ) )) }(db.classfile_methods) ∪
        Π{ ( (f : Field) =>  new class_member[AnyRef](f.declaringClass, new SourceElement[AnyRef]( f ) )) }(db.classfile_fields) ∪
        Π( (inner:inner_class) => new class_member[AnyRef](inner.source, new SourceElement[AnyRef]( inner.target ) ) ) (db.inner_classes)

    val direct_inner_class_members =
        (
                (db.inner_classes, (_ : inner_class).target.asReferenceType) ⋈ ( (_: class_member[AnyRef]).source ,direct_class_members)
        ){ (inner : inner_class, member: class_member[AnyRef]) => new class_member[AnyRef](inner.source, member.target) }

    val direct_inner_inner_class_members =
        (
                (db.inner_classes, (_ : inner_class).target.asReferenceType) ⋈ ( (_: class_member[AnyRef]).source ,direct_inner_class_members)
        ){ (inner : inner_class, member: class_member[AnyRef]) => new class_member[AnyRef](inner.source, member.target) }

    val class_members : LazyView[class_member[AnyRef]] =
        direct_class_members ∪
        // TODO make recursive/transitive
        direct_inner_class_members ∪
        direct_inner_inner_class_members


    def class_with_members(packageName : String, className : String) : LazyView[SourceElement[AnyRef]] = class_with_members(packageName + "." + className)

    private def fromJava(unresolved : String) : String = unresolved.replace('.', '/')
}


object Queries
{
    def source(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.source.asInstanceOf[AnyRef])

    def target(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.target.asInstanceOf[AnyRef])

}