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

    type FieldType = de.tud.cs.st.bat.resolved.FieldType

    type VoidType = de.tud.cs.st.bat.resolved.VoidType

    type ReferenceType = de.tud.cs.st.bat.resolved.ReferenceType

    type ClassType = de.tud.cs.st.bat.resolved.ObjectType

    type ArrayType = de.tud.cs.st.bat.resolved.ArrayType

    type InterfaceType = de.tud.cs.st.bat.resolved.ObjectType

    type PrimitiveType = de.tud.cs.st.bat.resolved.BaseType

    type AccessModified = {def accessFlags: Int}

    type ClassMember = de.tud.cs.st.bat.resolved.ClassMember with AccessModified {def declaringType : ClassType}

    type DeclaredClassMember = ClassMember {def declaringClass : ClassDeclaration}

    type ClassDeclaration = de.tud.cs.st.bat.resolved.ClassFile with AccessModified

    type InterfaceDeclaration = de.tud.cs.st.bat.resolved.ClassFile with AccessModified

    type MethodDeclaration = de.tud.cs.st.bat.resolved.Method with DeclaredClassMember

    type FieldDeclaration = de.tud.cs.st.bat.resolved.Field with DeclaredClassMember

    type FieldReference = ClassMember{def name:String; def fieldType : FieldType}

    type SourceElement = de.tud.cs.st.bat.resolved.SourceElement

    type ReadFieldInstruction = de.tud.cs.st.bat.resolved.Instruction{def declaringMethod: MethodDeclaration; def targetField : FieldReference}

}
