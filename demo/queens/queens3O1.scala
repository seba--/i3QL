package queens

import saere._

import saere.predicate._
import saere.term._
import saere.meta._
import saere.IntegerAtom.IntegerAtom
import saere.StringAtom._ 


object queens3O1 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
		new queens3O1p(a1,a2,a3)
	}
}

class queens3O1p (private val a1 : Term,private val a2 : Term, val a3:Term) extends MultipleRules { 
		

	// queens([],Qs,Qs).
	/*
	private class queens3O1c1 extends MultipleGoals {
			
		val goalCount : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.unify(a1,StringAtom.emptyList)
			case 2 => Unify2.unify(a2,a3)
		}
	}
	*/
	
	// queens([],Qs,Qs). => we have two goals: (1) test if the first term is the empty list and (2) if the
	// second and third parameter unify.... (btw, it is a semi-deterministic goal!)
	private class queens3O1c1 extends Solutions {
		
		private val a1State = a1 manifestState
		private var a2State : State = null
		private var a3State : State = null
				
		private var called = false
		
		def next() : Boolean =  {
			if (!called) {
				called = true
				if(a1.unify(emptyList)) {	
					// delay the state manifestation as long as possible...
					a2State = a2.manifestState
					a3State = a3.manifestState
					if (a2 unify a3) {
						return true
					} else {
						a2 setState a2State
						a3 setState a3State
						return false
					}
				} else {
					a1 setState a1State
					return false
				}
			}
			a1 setState a1State
			if (a2State != null) a2 setState a2State
			if (a3State != null)	a3 setState a3State
			false
		}
					
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1,emptyList)
			case 2 => Unify2.apply(a2,a3)
		}
	}
	
	
	// queens(UnplacedQs,SafeQs,Qs) :-
	// 	select(UnplacedQs,UnplacedQs1,Q),
	// 	not_attack(SafeQs,Q),
	// 	queens(UnplacedQs1,[Q|SafeQs],Qs).
	private class queens3O1c2 extends MultipleGoals {
	
		val UnplacedQs1 = new Variable 
		val Q = new Variable 
		
		def goalCount() : Int = 3
				
		def goal(i : Int ) : Solutions = i match {
			case 1 => select3O1(a1,UnplacedQs1,Q)
			// AFTER INLINING
//			case 2 => not_attack3O1TC(a2,Q,IntegerAtom(1))
			case 2 => not_attack3O1(a2,Q,IntegerAtom(1))
			case 3 => queens3O1(UnplacedQs1,new ListElement2(Q,a2),a3)
		}	
	}
		
	def ruleCount() = 2
	
	def rule(i : Int ) = i match {
		case 1 => new queens3O1c1
		case 2 => new queens3O1c2 
	}
}



