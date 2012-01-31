package sae.bytecode.model


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.01.12
 * Time: 14:10
 *
 */
trait MethodIdentifier
{

    def declaringRef: de.tud.cs.st.bat.ReferenceType

    def name: String

    def parameters: Seq[de.tud.cs.st.bat.Type]

    def returnType: de.tud.cs.st.bat.Type

    def isConstructor = {
        name.startsWith("<init>")
    }

    def isStaticInitializer = {
        name == "<clinit>"
    }

}