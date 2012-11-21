package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:53
 *
 */

case class implements(source: ObjectType, target: ObjectType)
        extends Dependency[ObjectType, ObjectType]
{


}