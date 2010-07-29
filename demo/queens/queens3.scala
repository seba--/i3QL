package queens

import saere._

import saere.predicate._
import saere.term._
import saere.meta._

object queens3 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
		new queens3p(a1,a2,a3)
	}
}

class queens3p (private val a1 : Term,private val a2 : Term, val a3:Term) extends MultipleRules { 
		

	// queens([],Qs,Qs).
	private class queens3c1 extends MultipleGoals {
			
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1,StringAtom.emptyList)
			case 2 => Unify2.apply(a2,a3)
		}
	}
	
	// queens(UnplacedQs,SafeQs,Qs) :-
	// 	select(UnplacedQs,UnplacedQs1,Q),
	// 	not_attack(SafeQs,Q),
	// 	queens(UnplacedQs1,[Q|SafeQs],Qs).
	private class queens3c2 extends MultipleGoals {
	
		val UnplacedQs1 = new Variable 
		val Q = new Variable 
		
		def goalCount() : Int = 3
				
		def goal(i : Int ) : Solutions = i match {
			case 1 => select3(a1,UnplacedQs1,Q)
			case 2 => not_attack2(a2,Q)
			case 3 => queens3(UnplacedQs1,new ListElement2(Q,a2),a3)
		}	
	}
		
	def ruleCount() = 2
	
	def rule(i : Int ) = i match {
		case 1 => new queens3c1
		case 2 => new queens3c2 
	}
}



