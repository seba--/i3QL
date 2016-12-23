package idb.syntax.iql

import java.text.SimpleDateFormat
import java.util.Date
import idb.syntax.iql.IR._

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object DATE {

	def apply(s : String) : Rep[Date] = {
		val sdf = new SimpleDateFormat("yyyy-MM-dd")
		sdf.parse(s)
	}
}
