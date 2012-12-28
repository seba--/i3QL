package unisson.model.dependencies


import de.tud.cs.st.vespucci.interfaces.ICodeElement
import unisson.model.kinds.primitive.ClassCastKind

/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:11
 *
 */
@deprecated
case class ClassCastDependency (source: ICodeElement, target: ICodeElement)
    extends Dependency
{
    def kind = ClassCastKind
}