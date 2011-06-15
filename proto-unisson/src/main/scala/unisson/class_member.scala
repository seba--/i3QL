package unisson

import sae.bytecode.model.dependencies.Dependency
import de.tud.cs.st.bat.ReferenceType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 10.06.11 13:58
 *
 */
case class class_member[T](source : ReferenceType, target : SourceElement[T])
    extends Dependency[ReferenceType, SourceElement[T]]