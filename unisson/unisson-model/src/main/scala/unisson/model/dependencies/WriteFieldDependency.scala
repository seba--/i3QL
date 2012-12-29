package unisson.model.dependencies


import de.tud.cs.st.vespucci.interfaces.ICodeElement
import unisson.model.kinds.primitive.WriteFieldKind

/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */
@deprecated
case class WriteFieldDependency(source: ICodeElement, target: ICodeElement)
    extends Dependency
{
    def kind = WriteFieldKind
}