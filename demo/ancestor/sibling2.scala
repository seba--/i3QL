package ancestor

import saere._
import saere.predicate._
import saere.meta._

object sibling2 {
	
	def unify(a1 : Term, a2 : Term) : Solutions = {
	
		new sibling2p(a1,a2)
		
	}
	
}


// sibling(X,Y) :- mother(M,X) , mother(M,Y),  X \= Y, father(F,X), father(F,Y).
class sibling2p(private val a1 : Term, val a2 : Term) extends Solutions { 
	
	/*
	private val a1State : State = a1.manifestState 
	private val a2State : State = a2.manifestState 
	*/
	
	private class sibling2c1 extends MultipleGoals {
	
		val g1v1 = new Variable // M
		val g4v1 = new Variable // F
			
		def goalCount() : Int = 5
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => mother2.unify(g1v1, a1)
			case 2 => mother2.unify(g1v1, a2)
			case 3 => NotUnify2.apply(a1, a2)
			case 4 => father2.unify(g4v1, a1)
			case 5 => father2.unify(g4v1, a2)
		}	
	}

	val solutions : Solutions = new sibling2c1 
	
	def next()  = solutions.next
	
}



