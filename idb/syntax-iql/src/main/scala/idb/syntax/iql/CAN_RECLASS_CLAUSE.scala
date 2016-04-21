package idb.syntax.iql

import idb.query.colors.Color
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
trait CAN_RECLASS_CLAUSE[Select, Range] {

	def RECLASS(
		newColor : Color
	) : RECLASS_CLAUSE[Select, Range]


}
