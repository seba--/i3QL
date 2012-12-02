package unisson.query.compiler

import unisson.query.code_model.SourceElement
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 10.06.11 13:58
 *
 */
private[compiler] case class ClassMember[+T <: AnyRef](outerType: ObjectType, member: SourceElement[T])