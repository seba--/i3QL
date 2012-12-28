package unisson.query.compiler

import de.tud.cs.st.bat.resolved._
import unisson.query.code_model.SourceElementFactory
import sae.MaterializedRelation

/**
 *
 * Author: Ralf Mitschke
 * Date: 19.02.12
 * Time: 10:49
 *
 */
class TypeElementView(name: String) extends MaterializedRelation[SourceElement[Type]]
{
    val typeElement = SourceElementFactory (TypeElementView.getType (name)).asInstanceOf[SourceElement[Type]]

    def lazyInitialize() {}

    /**
     * Applies f to all elements of the view with their counts
     */
    def foreachWithCount[T](f: (SourceElement[Type], Int) => T) {
        f (typeElement, 1)
    }


    def foreach[T](f: (SourceElement[Type]) => T) {
        f (typeElement)
    }


    def isDefinedAt(v: SourceElement[Type]) = (v == typeElement)

    /**
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the
     */
    def elementCountAt[T >: SourceElement[Type]](v: T) =
        if (v == typeElement)
            1
        else
            0

    def isSet = true

    /*
override def createIndex[K <: AnyRef](keyFunction: SourceElement[Type] => K) : Index[K, SourceElement[Type]] = new TypeElementIndex (keyFunction)

class TypeElementIndex[K <: AnyRef](val keyFunction: SourceElement[Type] => K) extends Index[K, SourceElement[Type]]
{

 /*
 override def lazyInitialize() {
     /* do nothing*/
 }

 val elementKey = f (typeElement)

 protected def materialized_foreach[T](f: ((K, SourceElement[Type])) => T) {
     f (elementKey, typeElement)
 }

 protected def materialized_size = 1

 protected def materialized_singletonValue = Some ((elementKey, typeElement))

 protected def materialized_contains(v: (K, SourceElement[Type])) = v == (elementKey, typeElement)

 def relation = TypeElementView.this

 def keyFunction = f

 protected def foreachKey(f: (K) => U) {}

 protected def put_internal(key: K, value: SourceElement[Type]) {
     throw new UnsupportedOperationException
 }

 protected def get_internal(key: K) = if (key == elementKey) Some (Seq (typeElement)) else None

 protected def isDefinedAt_internal(key: K) = key == elementKey

 protected def elementCountAt_internal(key: K) = if (key == elementKey) 1 else 0

 */

 val elementKey = keyFunction (typeElement)

 def relation = TypeElementView.this

 def size = 1

 def foreachKey[U](f: (K) => U) {
     f (elementKey)
 }

 def foreach[T](f: ((K, SourceElement[Type])) => T) {
     f (elementKey, typeElement)
 }

 def get(key: K) = if (key == elementKey) Some (Seq (typeElement)) else None

 def isDefinedAt(key: K) = key == elementKey

 def elementCountAt(key: K) = if (key == elementKey) 1 else 0

 def put(key: K, value: SourceElement[Type]) {
     throw new UnsupportedOperationException
 }

 def add_element(key: K, value: SourceElement[Type]) {
     throw new UnsupportedOperationException
 }

 def remove_element(key: K, value: SourceElement[Type]) {
     throw new UnsupportedOperationException
 }

 def update_element(oldKey: K, oldV: SourceElement[Type], newKey: K, newV: SourceElement[Type]) {
     throw new UnsupportedOperationException
 }

 def updated[U <: SourceElement[Type]](update: Update[U]) {
     throw new UnsupportedOperationException
 }

 def modified[U <: SourceElement[Type]](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
     throw new UnsupportedOperationException
 }

}
    */
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