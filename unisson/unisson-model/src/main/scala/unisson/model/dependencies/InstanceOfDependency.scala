package unisson.model.dependencies


import de.tud.cs.st.vespucci.interfaces.ICodeElement
import unisson.model.kinds.primitive.InstanceOfKind

/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:11
 *
 */
@deprecated
case class InstanceOfDependency(source: ICodeElement, target: ICodeElement)
    extends Dependency
{
    def kind = InstanceOfKind
}