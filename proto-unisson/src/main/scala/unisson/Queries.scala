package unisson

import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Field, Method}
import sae.syntax.RelationalAlgebraSyntax._
import sae.LazyView
import sae.bytecode.model.dependencies.Dependency

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


    def class_with_members(qualifiedClass : String) : LazyView[SourceElement[AnyRef]] =
        (
             Π[ObjectType, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:ObjectType)) }(σ{ (_:ObjectType) == ObjectType(fromJava(qualifiedClass))}(db.classfiles))
        ) ∪
        (
            Π[Method, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:Method)) } (σ{ (_:Method).declaringRef == ObjectType(fromJava(qualifiedClass)) }(db.classfile_methods))
        ) ∪
        (
            Π[Field, SourceElement[AnyRef]]{ SourceElement[AnyRef]((_:Field)) } (σ{ (_:Field).declaringClass == ObjectType(fromJava(qualifiedClass)) }(db.classfile_fields))
        )

    def class_with_members(packageName : String, className : String) : LazyView[SourceElement[AnyRef]] = class_with_members(packageName + "." + className)

    private def fromJava(unresolved : String) : String = unresolved.replace('.', '/')
}


object Queries
{
    def source(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.source.asInstanceOf[AnyRef])

    def target(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.target.asInstanceOf[AnyRef])

}