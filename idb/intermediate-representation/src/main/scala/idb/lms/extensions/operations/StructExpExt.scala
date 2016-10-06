package idb.lms.extensions.operations

import scala.reflect.RefinedManifest
import scala.virtualization.lms.common.StructExp

/**
 * @author Mirko Köhler
 */
trait StructExpExt extends StructExp {

	override def structName[T](m: Manifest[T]): String = m match {
		// FIXME: move to codegen? we should be able to have different policies/naming schemes
		case rm: RefinedManifest[_] => super.structName[T](m)
		//case _ if (m <:< manifest[AnyVal]) => m.toString
		case _ if m.erasure.isArray => super.structName[T](m)
		case _ if m.typeArguments.size > 0 => m.toString() //+ m.typeArguments.map(t => structName(t)).mkString("[",",","]")
		case _ => super.structName(m)
	}

}
