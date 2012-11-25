package sandbox.dataflowAnalysis

import sae.bytecode.structure.MethodDeclaration
import scala.Array

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 02.11.12
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
case class MethodTransformer[T](declaringMethod: MethodDeclaration, generators: Array[T => T]) {

}
