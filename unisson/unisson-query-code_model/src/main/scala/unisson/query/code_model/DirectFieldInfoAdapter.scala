package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.{SourceElement, IFieldDeclaration}
import sae.bytecode.structure.{FieldComparison, FieldInfo}
import de.tud.cs.st.bat.resolved.{FieldType, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 12:48
 *
 */
class DirectFieldInfoAdapter(val declaringType: ObjectType,
                             val name: String,
                             val fieldType: FieldType)
    extends IFieldDeclaration with SourceElement[FieldInfo] with FieldComparison
{
    def this (element: FieldInfo) = this (element.declaringType, element.name, element.fieldType)

    def getPackageIdentifier = declaringType.packageName

    def getSimpleClassName = declaringType.simpleName

    def getLineNumber = -1

    override def equals(obj: Any): Boolean = {
        if (obj.isInstanceOf[IFieldDeclaration]) {
            val other = obj.asInstanceOf[IFieldDeclaration]
            return this.getPackageIdentifier == other.getPackageIdentifier &&
                this.getSimpleClassName == other.getSimpleClassName &&
                this.getFieldName == other.getFieldName &&
                this.getTypeQualifier == other.getTypeQualifier
        }
        super.equals (obj)
    }


    def getFieldName = name

    def getTypeQualifier = fieldType.toJava

    override def toString = declaringType.toJava +
        name +
        ":" + fieldType.toJava

    def element = this
}