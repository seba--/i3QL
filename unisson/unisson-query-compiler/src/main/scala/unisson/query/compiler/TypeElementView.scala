package unisson.query.compiler

import de.tud.cs.st.bat.Type
import de.tud.cs.st.bat.VoidType
import de.tud.cs.st.bat.ByteType
import de.tud.cs.st.bat.CharType
import de.tud.cs.st.bat.DoubleType
import de.tud.cs.st.bat.FloatType
import de.tud.cs.st.bat.IntegerType
import de.tud.cs.st.bat.LongType
import de.tud.cs.st.bat.ShortType
import de.tud.cs.st.bat.BooleanType
import de.tud.cs.st.bat.ArrayType
import de.tud.cs.st.bat.ObjectType
import unisson.query.code_model.SourceElement
import sae.{Index, IndexedView}

/**
 *
 * Author: Ralf Mitschke
 * Date: 19.02.12
 * Time: 10:49
 *
 */
class TypeElementView(name: String) extends IndexedView[SourceElement[Type]]
{
    val typeElement = SourceElement(TypeElementView.getType(name)).asInstanceOf[SourceElement[Type]]

    def lazyInitialize() {
        /* do nothing*/
    }

    protected def materialized_foreach[T](f: (SourceElement[Type]) => T) {
        f(typeElement)
    }

    protected def materialized_size = 1

    protected def materialized_singletonValue = Some(typeElement)

    protected def materialized_contains(v: SourceElement[Type]) = v == typeElement

    protected def createIndex[K <: AnyRef](keyFunction: (SourceElement[Type]) => K) = new TypeElementIndex(keyFunction)

    class TypeElementIndex[K <: AnyRef](val f: SourceElement[Type] => K) extends Index[K, SourceElement[Type]]
    {
        override def lazyInitialize() {/* do nothing*/}

        val elementKey = f(typeElement)

        protected def materialized_foreach[T](f: ((K, SourceElement[Type])) => T) {
            f(elementKey, typeElement)
        }

        protected def materialized_size = 1

        protected def materialized_singletonValue = Some((elementKey, typeElement))

        protected def materialized_contains(v: (K, SourceElement[Type])) = v ==(elementKey, typeElement)

        def relation = TypeElementView.this

        def keyFunction = f

        protected def foreachKey_internal[U](f: (K) => U) {}

        protected def put_internal(key: K, value: SourceElement[Type]) {
            throw new UnsupportedOperationException
        }

        protected def get_internal(key: K) = if (key == elementKey) Some(Seq(typeElement)) else None

        protected def isDefinedAt_internal(key: K) = key == elementKey

        protected def elementCountAt_internal(key: K) = if (key == elementKey) 1 else 0

        def add_element(kv: (K, SourceElement[Type])) {
            throw new UnsupportedOperationException
        }

        def remove_element(kv: (K, SourceElement[Type])) {
            throw new UnsupportedOperationException
        }

        def update_element(oldKey: K, oldV: SourceElement[Type], newKey: K, newV: SourceElement[Type]) {
            throw new UnsupportedOperationException
        }
    }

}

object TypeElementView
{
    def getType(name: String): Type = {
        name match {
            case "void" => VoidType()
            case "byte" => ByteType()
            case "char" => CharType()
            case "double" => DoubleType()
            case "float" => FloatType()
            case "int" => IntegerType()
            case "long" => LongType()
            case "short" => ShortType()
            case "boolean" => BooleanType()
            case ArrayTypeRegEx(componentTypeDescriptor, arrayParens) => {
                val componentType = getType(componentTypeDescriptor).asFieldType
                val dimension = arrayParens.length() / 2
                ArrayType(dimension, componentType)
            }
            case _ => {// not primitive not array, must be object type
                val o = ObjectType(name.replaceAll("\\.", "/"))
                o
            }
        }
    }

    private val ArrayTypeRegEx = """(.+?)((?:\[\])+)""".r
}