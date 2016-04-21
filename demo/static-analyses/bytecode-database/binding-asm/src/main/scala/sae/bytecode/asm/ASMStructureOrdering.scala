package sae.bytecode.asm

import sae.bytecode.structure.base.BytecodeStructureOrdering


/**
 *
 * @author Ralf Mitschke
 *
 */
trait ASMStructureOrdering
    extends BytecodeStructureOrdering
    with ASMStructure
    with ASMTypeOrdering
{


    implicit def classDeclarationOrdering (): Ordering[ClassDeclaration] =
        new Ordering[ClassDeclaration]
        {
            def compare (x: ClassDeclaration, y: ClassDeclaration): Int =
                typeOrdering ().compare (x.classType, y.classType)
        }

    implicit def fieldDeclarationOrdering (): Ordering[FieldDeclaration] =
        new Ordering[FieldDeclaration]
        {
            def compare (x: FieldDeclaration, y: FieldDeclaration): Int = {
                val classComp = classDeclarationOrdering ().compare (x.declaringClass, y.declaringClass)
                if (classComp != 0) return classComp
                val nameComp = x.name.compareTo (y.name)
                if (nameComp != 0) return nameComp
                val fieldTypeComp = typeOrdering ().compare (x.fieldType, y.fieldType)
                if (fieldTypeComp != 0) return fieldTypeComp
                0
            }

        }

    implicit def methodDeclarationOrdering (): Ordering[MethodDeclaration] =
        new Ordering[MethodDeclaration]
        {
            def compare (x: MethodDeclaration, y: MethodDeclaration): Int = {
                val classComp = classDeclarationOrdering ().compare (x.declaringClass, y.declaringClass)
                if (classComp != 0) return classComp
                val nameComp = x.name.compareTo (y.name)
                if (nameComp != 0) return nameComp
                val returnTypeComp = typeOrdering ().compare (x.returnType, y.returnType)
                if (returnTypeComp != 0) return returnTypeComp
                x.parameterTypes.zip (y.parameterTypes).foreach (
                    pair => {
                        val parameterTypeComp = typeOrdering ().compare (pair._1, pair._2)
                        if (parameterTypeComp != 0) return returnTypeComp
                    }
                )
                0
            }
        }
}
