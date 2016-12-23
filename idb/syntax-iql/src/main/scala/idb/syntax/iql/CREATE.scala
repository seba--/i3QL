package idb.syntax.iql

import idb.query.QueryEnvironment
import idb.query.taint.Taint
import idb.{BagTable, SetTable}
import idb.syntax.iql.IR.{Query, _}

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object CREATE {
	//def OF[Domain](clazz : Class[Domain]) : Table[Domain] = _
	def TABLE[Domain : Manifest](taint : Taint = Taint.NO_TAINT)(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		table[Domain](BagTable.empty[Domain], isSet = false, taint = taint)

	def SET[Domain : Manifest](taint : Taint = Taint.NO_TAINT)(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		table[Domain](SetTable.empty[Domain], isSet = true, taint = taint)
}
