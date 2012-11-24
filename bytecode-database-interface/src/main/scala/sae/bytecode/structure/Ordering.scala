package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * @author Ralf Mitschke
 *
 */
object Ordering
{

    implicit def typeOrdering: Ordering[Type] =
        new Ordering[Type]
        {
            def compare(x: Type, y: Type): Int = {
                x.toJava.compareTo(y.toJava)
            }
        }

    implicit def classDeclarationOrdering(implicit typeOrdering: Ordering[Type]
                                                 ): Ordering[ClassDeclaration] =
        new Ordering[ClassDeclaration]
        {
            def compare(x: ClassDeclaration, y: ClassDeclaration): Int = {
                typeOrdering.compare(x.classType, y.classType)
            }
        }

    implicit def methodDeclarationOrdering(implicit typeOrdering: Ordering[Type],
                                           classDeclarationOrdering: Ordering[ClassDeclaration]
                                                  ): Ordering[MethodDeclaration] =
        new Ordering[MethodDeclaration]
        {
            def compare(x: MethodDeclaration, y: MethodDeclaration): Int = {
                val classOrder = classDeclarationOrdering.compare(x.declaringClass, y.declaringClass)
                if (classOrder != 0) return classOrder

                val nameOrder = x.name.compareTo(y.name)
                if (nameOrder != 0) return nameOrder

                val paramOrder = x.parameterTypes.map(_.toJava).reduce(_ + _).compareTo(
                    y.parameterTypes.map(_.toJava).reduce(_ + _)
                )
                if (paramOrder != 0) return paramOrder

                typeOrdering.compare(x.returnType, y.returnType)
            }
        }

}