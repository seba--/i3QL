package sandbox.dataflowAnalysis

import sae.bytecode.structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 02.11.12
 * Time: 20:47
 * To change this template use File | Settings | File Templates.
 */
case class MethodResult[T](declaringMethod: MethodDeclaration, resultArray: Array[T]) {

}
