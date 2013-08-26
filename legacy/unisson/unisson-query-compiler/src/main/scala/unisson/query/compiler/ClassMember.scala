package unisson.query.compiler

import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.vespucci.interfaces.ICodeElement

/**
 *
 * Author: Ralf Mitschke
 * Created: 10.06.11 13:58
 *
 */
private[compiler] case class ClassMember(outerType: ObjectType, member: ICodeElement)