package queens

import saere._
import saere.meta._

import saere.predicate._
import saere.term._


object not_attack3 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
		new not_attack3p(a1,a2,a3)
	}
}

class not_attack3p (private val a1 : Term,private val a2 : Term, val a3:Term) extends MultipleRules { 
		
	/*
	// not_attack([],_,_) :- !. // TODO Support cuts.
	private class not_attack3c1 extends OneGoal {
			
		def goal() : Solutions = Unify2.unify(a1, StringAtom.emptyList)
	}
	*/
	
	// not_attack([Y|Ys],X,N) :-
	// 	X =\= Y+N, X =\= Y-N,
	// 	N1 is N+1,
	// 	not_attack(Ys,X,N1).
	// ...
	private final class not_attack3c2 extends MultipleGoals {
	
		private var Y : Variable = null
		private var Ys : Variable = null

		private var N1 : Variable = null
		
		def goalCount : Int = 4
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Y = new Variable; Ys = new Variable; Unify2(a1,new ListElement2(Y,Ys))
			case 2 => new Once{
				def eval() = 
					NotSame2.isNotSame(a2,new Add2(Y,a3)) && NotSame2.isNotSame(a2,new Subtract2(Y,a3))
			}
			case 3 => N1 = new Variable; Is2(N1,new Add2(a3,IntegerAtom(1)))
			case 4 => not_attack3(Ys,a2,N1)
		}	
	}
		
	val ruleCount = 2
	
	def rule(i : Int ) = i match {
		case 1 => Unify2(a1, StringAtom.emptyList) /*new not_attack3c1*/
		case 2 => new not_attack3c2 
	}
}



