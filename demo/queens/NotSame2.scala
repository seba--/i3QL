package saere.op.arith

import saere._


/** Prolog's arithmetic "not equals" operator: "=\=". */
object NotSame2  {
		
	def isNotSame (a1 : Term, a2 : Term) : Boolean = {
		a1.eval != a2.eval
	}
}

