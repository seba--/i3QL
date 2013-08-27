package unisson.query.compiler

import de.tud.cs.st.bat.resolved._
import unisson.query.code_model.SourceElementFactory
import sae.MaterializedRelation
import de.tud.cs.st.vespucci.interfaces.ICodeElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 19.02.12
 * Time: 10:49
 *
 */
class TypeElementView(name: String) extends MaterializedRelation[ICodeElement]
{
    val typeElement = SourceElementFactory (TypeElementView.getType (name))

    def lazyInitialize() {}

    /**
     * Applies f to all elements of the view with their counts
     */
    def foreachWithCount[T](f: (ICodeElement, Int) => T) {
        f (typeElement, 1)
    }


    def foreach[T](f: (ICodeElement) => T) {
        f (typeElement)
    }


    def isDefinedAt(v: ICodeElement) = (v == typeElement)

    /**
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the
     */
    def elementCountAt[T >: ICodeElement](v: T) =
        if (v == typeElement)
            1
        else
            0

    def isSet = true

}

// TODO return SourceElement directly and only a singleton for each primitive type
object TypeElementView
{
    def getType(name: String): Type = {
        name match {
            case "void" => VoidType
            case "byte" => ByteType
            case "char" => CharType
            case "double" => DoubleType
            case "float" => FloatType
            case "int" => IntegerType
            case "long" => LongType
            case "short" => ShortType
            case "boolean" => BooleanType
            case ArrayTypeRegEx (componentTypeDescriptor, arrayParens) => {
                val componentType = getType (componentTypeDescriptor).asInstanceOf[FieldType]
                val dimension = arrayParens.length () / 2
                ArrayType (dimension, componentType)
            }
            case _ => {
                // not primitive not array, must be object type
                val o = ObjectType (name.replaceAll ("\\.", "/"))
                o
            }
        }
    }

    private val ArrayTypeRegEx = """(.+?)((?:\[\])+)""".r
}