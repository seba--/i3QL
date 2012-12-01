package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.ICodeElement
import sae.bytecode.model.{MethodReference, FieldDeclaration, MethodDeclaration, FieldReference}
import de.tud.cs.st.bat.resolved.{Type, ArrayType, ObjectType}
import soot.bridge.ISootCodeElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 14:07
 *
 */
trait SourceElement[+T <: AnyRef]
        extends ICodeElement with ISootCodeElement
{
    def element: T
}

object SourceElement
{
    def apply[T <: AnyRef](element: T): SourceElement[_ <: AnyRef] = {
        if (element.isInstanceOf[ObjectType]) {
            return new ClassDeclaration(element.asInstanceOf[ObjectType])
        }
        if (element.isInstanceOf[MethodDeclaration]) {
            return new MethodDeclarationAdapter(element.asInstanceOf[MethodDeclaration])
        }
        if (element.isInstanceOf[MethodReference]) {
            return new MethodReferenceAdapter(element.asInstanceOf[MethodReference])
        }
        if (element.isInstanceOf[FieldDeclaration]) {
            return new FieldDeclarationAdapter(element.asInstanceOf[FieldDeclaration])
        }
        if (element.isInstanceOf[FieldReference]) {
            return new FieldReferenceAdapter(element.asInstanceOf[FieldReference])
        }
        if (element.isInstanceOf[Type]) {
            return new TypeReference(element.asInstanceOf[Type])
        }

        throw new IllegalArgumentException("can not convert " + element + " to a SourceElement")
    }

    def unapply[T <: AnyRef](sourceElement: SourceElement[T]): Option[T] = {
        Some(sourceElement.element)
    }

    // TODO careful with to string, use for testing only
    implicit def compare[T <: AnyRef](x: SourceElement[T], y: SourceElement[T]): Int = {
        if (x.isInstanceOf[ClassDeclaration] && y.isInstanceOf[ClassDeclaration]) {
            return x.asInstanceOf[ClassDeclaration].getTypeQualifier
                    .compare(y.asInstanceOf[ClassDeclaration].getTypeQualifier)
        }

        x.toString.compareTo(y.toString)
    }

    implicit def ordering[T <: AnyRef]: Ordering[SourceElement[T]] = new Ordering[SourceElement[T]] {
        def compare(x: SourceElement[T], y: SourceElement[T]) = SourceElement.compare(x, y)
    }
}