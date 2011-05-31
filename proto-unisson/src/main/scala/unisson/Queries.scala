package unisson

import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Field, Method}
import sae.syntax.RelationalAlgebraSyntax._
import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 13:37
 *
 */
class Queries( val db : BytecodeDatabase )
{
    def `package`(name : String) : LazyView[SourceElement[_]] =
        (
             Π[ObjectType, SourceElement[_]]{ SourceElement[ObjectType]((_:ObjectType)) }(σ{ (_:ObjectType).packageName == fromJava(name)}(db.classfiles))
        ) ∪
        (
            Π[Method, SourceElement[_]]{ SourceElement[Method]((_:Method)) } (σ{ (_:Method).declaringRef.packageName == fromJava(name) }(db.classfile_methods))
        ) ∪
        (
            Π[Field, SourceElement[_]]{ SourceElement[Field]((_:Field)) } (σ{ (_:Field).declaringClass.packageName == fromJava(name) }(db.classfile_fields))
        )


    def class_with_members(qualifiedClass : String) : LazyView[SourceElement[_]] =
        (
             Π[ObjectType, SourceElement[_]]{ SourceElement[ObjectType]((_:ObjectType)) }(σ{ (_:ObjectType) == ObjectType(fromJava(qualifiedClass))}(db.classfiles))
        ) ∪
        (
            Π[Method, SourceElement[_]]{ SourceElement[Method]((_:Method)) } (σ{ (_:Method).declaringRef == ObjectType(fromJava(qualifiedClass)) }(db.classfile_methods))
        ) ∪
        (
            Π[Field, SourceElement[_]]{ SourceElement[Field]((_:Field)) } (σ{ (_:Field).declaringClass == ObjectType(fromJava(qualifiedClass)) }(db.classfile_fields))
        )

    def fromJava(unresolved : String) : String = unresolved.replace('.', '/')
}