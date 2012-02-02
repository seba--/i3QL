package unisson.query.compiler

import sae.bytecode.model.dependencies.Dependency
import de.tud.cs.st.bat.ReferenceType
import unisson.query.code_model.SourceElement

/**
 *
 * Author: Ralf Mitschke
 * Created: 10.06.11 13:58
 *
 */
private[compiler] case class class_member[+T <: AnyRef](source: ReferenceType, target: SourceElement[T])
        extends Dependency[ReferenceType, SourceElement[T]]