package ancestor

import saere._
import saere.predicate._
import saere.meta._
import saere.StringAtom.StringAtom

object female1 {
	
	def unify(a1 : Term) : Solutions = {
	
		new female1pSolutions(a1)
		
	}
	
	// Possible additional methods	
	// lookup (
	//	If we know that a fact's key is bound and one or more values need to be bound.
	//  In this case, the caller has to take of the state handling functionality.
	// )
	// exists ( if all values are bound and a simple test is made whether a certain value exists)
}


// Solutions when unifying with the predicate female/1
class female1pSolutions(private val a1 : Term) extends MultipleRules { 
		
	private val a1State : State = a1.manifestState // Possible Optimization: only manifest the state when necessary - i.e., when the particular term is actually unified
	
	private class female1c1 extends OneGoal {
	
		val t : Term = StringAtom("Leonie")
		
		def goal = Unify2(a1, t)	
	}
	private class female1c2 extends OneGoal {
	
		val t : Term = StringAtom("Valerie")
	
		def goal = Unify2(a1, t)		
	}
	private class female1c3 extends OneGoal {
	
		val t : Term = StringAtom("Alice")
	
		def goal = Unify2(a1, t)		
	}	
	private class female1c4 extends OneGoal {
	
		val t : Term = StringAtom("Christel")
	
		def goal = Unify2(a1, t)		
	}	
	private class female1c5 extends OneGoal {
	
		val t : Term = StringAtom("Heidi")
	
		def goal = Unify2(a1, t)		
	}
	private class female1c6 extends OneGoal {
	
		val t : Term = StringAtom("Magdalena")
	
		def goal = Unify2(a1, t)		
	}

	val ruleCount: Int = 6
	
	def rule(i : Int) = i match {
		case 1 => new female1c1 
		case 2 => new female1c2
		case 3 => new female1c3
		case 4 => new female1c4
		case 5 => new female1c5
		case 6 => new female1c6
	}
}



