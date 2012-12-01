package unisson.query.compiler

import sae.bytecode.model.dependencies.Dependency
import unisson.query.code_model.SourceElement
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 10.06.11 13:58
 *
 */
private[compiler] case class class_member[+T <: AnyRef](source: ObjectType, target: SourceElement[T])
    extends Dependency[ObjectType, SourceElement[T]]