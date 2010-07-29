package ancestor

import saere._
import saere.predicate._
import saere.meta._
import saere.StringAtom.StringAtom

object male1 {
	
	def unify(a1 : Term) : Solutions = {
	
		new male1pSolutions(a1)
		
	}
	
	// Possible additional methods	
	// lookup (
	//	If we know that a fact's key is bound and one or more values need to be bound.
	//  In this case, the caller has to take of the state handling functionality.
	// )
	// exists ( if all values are bound and a simple test is made whether a certain value exists)
}


// Solutions when unifying with the predicate male/1
class male1pSolutions(private val a1 : Term) extends MultipleRules { 
		
	// private val a1State : State = a1.manifestState // Possible Optimization: only manifest the state when necessary - i.e., when the particular term is actually unified
	
	// male(X) :- X = 'Thilo' 
	private class male1c1 extends OneGoal {
	
		val t : Term = StringAtom("Thilo")
	
		def goal = Unify2.apply(a1, t)

	}
	private class male1c2 extends OneGoal {
	
		val t : Term = StringAtom("Michael")
	
		def goal = Unify2.apply(a1, t)
	
	}
	private class male1c3 extends OneGoal {
	
		val t : Term = StringAtom("Werner")
	
		def goal = Unify2.apply(a1, t)

	}	
	private class male1c4 extends OneGoal {
	
		// male(X) :- X = 'Reinhard' % unification with the argument
		val t : Term = StringAtom("Reinhard")
	
		def goal = Unify2.apply(a1,t)
	
	}
	/*
	private class male1c4Solutions extends Solutions {
	
		// male(X) :- X = 'Reinhard' % unification with the argument
		val t : Term = StringAtom("Reinhard")
	
		var finished = false
		
		def next() : Boolean = {
			// "after inlining"
			if (finished) {
				a1 setState a1State
				false
			} else {
				finished = true // there will be at most one solution...
				a1 unify t				
			}
		}	
	}
	*/		
	
	val ruleCount : Int = 4
		
	def rule(i : Int) = i match {
		case 1 => new male1c1
		case 2 => new male1c2
		case 3 => new male1c3
		case 4 => new male1c4
	}
		
}



