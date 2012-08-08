package sae.bytecode

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 07.08.12
 * Time: 11:20
 *
 */
trait BytecodeBaseRelations
{

    type Instruction

    type ReferenceType

    type ClassType <: ReferenceType

    type ArrayType <: ReferenceType

    type InterfaceType <: ReferenceType

    type PrimitiveType

    type ClassDeclaration

    type InterfaceDeclaration

    type MethodDeclaration

    type FieldDeclaration

    def declared_classes: LazyView[ClassDeclaration]

    def declared_methods: LazyView[MethodDeclaration]

    def declared_fields: LazyView[FieldDeclaration]

    def instructions: LazyView[Instruction]

}