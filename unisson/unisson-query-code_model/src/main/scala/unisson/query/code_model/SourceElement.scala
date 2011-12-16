package unisson.query.code_model

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Method, Field}

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 14:07
 *
 */
trait SourceElement[+T <: AnyRef]
{
    def element: T
}

object SourceElement
{
    def apply[T <: AnyRef](element: T): SourceElement[_ <: AnyRef] = {
        if (element.getClass == ObjectType.getClass) {
            new ClassDeclaration(element.asInstanceOf[ObjectType])
        }
        else if (element.getClass == Method.getClass) {
            new MethodDeclaration(element.asInstanceOf[Method])
        }
        else if (element.getClass == Field.getClass) {
            new FieldDeclaration(element.asInstanceOf[Field])
        }
        else {
            throw new IllegalArgumentException("can not convert " + element + " to a SourceElement")
        }

    }

    def unapply[T <: AnyRef](sourceElement: SourceElement[T]): Option[T] = {
        Some(sourceElement.element)
    }

}