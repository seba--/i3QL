package sae

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.08.12
 * Time: 22:41
 */
trait TypeBindingBAT
{
    type Instruction = de.tud.cs.st.bat.resolved.Instruction

    type Type = de.tud.cs.st.bat.resolved.Type

    type ReturnType = de.tud.cs.st.bat.resolved.Type

    type ParameterType = de.tud.cs.st.bat.resolved.FieldType

    type VoidType = de.tud.cs.st.bat.resolved.VoidType

    type ReferenceType = de.tud.cs.st.bat.resolved.ReferenceType

    type ClassType = de.tud.cs.st.bat.resolved.ObjectType

    type ArrayType = de.tud.cs.st.bat.resolved.ArrayType

    type InterfaceType = de.tud.cs.st.bat.resolved.ObjectType

    type PrimitiveType = de.tud.cs.st.bat.resolved.BaseType

    type ClassDeclaration = de.tud.cs.st.bat.resolved.ClassFile

    type InterfaceDeclaration = de.tud.cs.st.bat.resolved.ClassFile

    type MethodDeclaration = de.tud.cs.st.bat.resolved.Method

    type FieldDeclaration = de.tud.cs.st.bat.resolved.Field

    type ClassMember = de.tud.cs.st.bat.resolved.ClassMember

    type SourceElement = de.tud.cs.st.bat.resolved.SourceElement


}
