package queens

import saere._

import saere.predicate._
import saere.term._
import saere.meta._

object select3 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
	
		new select3p(a1,a2,a3)
	}
}

final class select3p (
		private val a1 : Term,
		private val a2 : Term,
		private val a3 : Term
) extends MultipleRules { 
		
	// select([X|Xs],Xs,X).
	/*
	private class select3c1 extends OneGoal {
	
		def goal() : Solutions = Unify2.unify(a1,new ListElement2(a3,a2) )
	} 
	*/
	/*private class select3c1 extends MultipleGoals {

		val X = new Variable	
		val Xs = new Variable
		val g1t1 = new ListElement2(X,Xs)

		def goalCount : Int = 3

		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.unify(a1, g1t1)
			case 2 => Unify2.unify(a2, Xs)
			case 3 => Unify2.unify(a3, X)
		}
		
	}*/


	// select([Y|Ys],[Y|Zs],X) :- select(Ys,Zs,X).
	// <=> select(A1,A2,X) :- A1=[Y|Ys], A2=[Y|Zs], select(Ys,Zs,X).
	/* STANDARD SOLUTION
	private class select3c2 extends MultipleGoals {
	
		// required by the first goal..
		private val Y = new Variable 
		private val Ys = new Variable 
		
		private var Zs : Variable = null 
		
		def goalCount : Int = 3
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.unify(a1, new ListElement2(Y,Ys))
			case 2 => Zs = new Variable ; Unify2.unify(a2, new ListElement2(Y,Zs))
			case 3 => select3.unify(Ys,Zs,a3)
		}	
	}
	*/

	// select([Y|Ys],[Y|Zs],X) :- select(Ys,Zs,X).
	// <=> select(A1,A2,X) :- A1=[Y|Ys], A2=[Y|Zs], select(Ys,Zs,X).
	// MANUALLY OPTIMIZED
	private class select3c2 extends Solutions {
	
		// required by the first goal..
		private val a1State = a1 manifestState
		private val Y = new Variable 
		private val Ys = new Variable 


		private var a2State : State = null
		private var Zs : Variable = null 
			
		private var solutions : Solutions = null	
			
		private var setup = false
		
		def next() : Boolean = {
			// TODO... how can we generate code as in the following
			// Idea: Using a switch over the currentGoal, where all (semi-)deterministic goals are
			// folded together
			if (!setup) {
				setup = true
				
				if (!(a1 unify new ListElement2(Y,Ys))){
					a1 setState a1State
					return false
				} else {
					a2State = a2.manifestState
					Zs = new Variable 
					
					if (!(a2 unify new ListElement2(Y,Zs))) {
						a2 setState a2State
						return false
					}
				}
				solutions = select3(Ys,Zs,a3)
			} 
			if (!solutions.next) {
				a1 setState a1State
				a2 setState a2State
				return false
			}
			
			true
		}
	}

		
	val ruleCount = 2
	
	def rule(i : Int ) = i match {
		case 1 => /*println("select3c1: "+a1+", "+a2+", "+a3);*/ Unify2(a1,new ListElement2(a3,a2) ) /* new select3c1 */
		case 2 => /*println("select3c2: "+a1+", "+a2+", "+a3);*/ new select3c2 
	}
}



