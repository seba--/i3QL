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

    private def fromJava(unresolved : String) : String = unresolved.replace('.', '/')
}


object Queries
{
    def source(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.source.asInstanceOf[AnyRef])

    def target(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.target.asInstanceOf[AnyRef])

}