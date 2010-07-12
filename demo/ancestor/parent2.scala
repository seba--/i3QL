package ancestor

import saere._
import saere.meta._

/*
	parent(X,Y) :- mother(X,Y).
	parent(X,Y) :- father(X,Y).
*/
object parent2 {
	
	def unify(a1 : Term,a2:Term) : Solutions = {
	
		new parent2p(a1,a2)
		
	}
}

class parent2p(private val a1 : Term,private val a2 : Term) extends MultipleRules { 
		
	/*
	private val a1State : State = a1.manifestState // Possible Optimization: only manifest the state when necessary - i.e., when the particular term is actually unified
	private val a2State : State = a2.manifestState // Possible Optimization: only manifest the state when necessary - i.e., when the particular term is actually unified
	*/	
	
	private class parent2c1 extends OneGoal {
	
		def goal = mother2.unify(a1, a2)
	}
	private class parent2c2 extends OneGoal {
			
		def goal = father2.unify(a1, a2) 	
	}

	val ruleCount = 2
	
	def rule(i : Int ) = i match {
		case 1 => new parent2c1
		case 2 => new parent2c2 
	}
}



