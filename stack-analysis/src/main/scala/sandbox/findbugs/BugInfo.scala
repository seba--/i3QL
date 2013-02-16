package sandbox.findbugs

import sae.bytecode.structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 15.02.13
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
case class BugInfo(declaringMethod : MethodDeclaration, pc : Int, bugType : BugType.Value) {

}
