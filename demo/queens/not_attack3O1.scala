package queens

import saere._

import saere.predicate._
import saere.term._


object not_attack3O1 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
		new not_attack3O1p(a1,a2,a3)
	}
}

private final class not_attack3O1p (private val a1 : Term,private val a2 : Term,private val a3 : Term) extends Solutions { 
	
	private var a1State : State = null
	private var a2State : State = null
	private var a3State : State = null

	private var Y : Variable = null
	private var Ys : Variable = null
	private var N1 : Variable = null

	private var cut = false	
	private var currentClause = 1
	private var currentGoal = 0 // i.e., "no initialization has been done"

	private var goalStack : List[Solutions] = Nil

	
	def next() : Boolean = {
		
		currentClause match {
			case 1 => { 
				if (c1()) {
					return true
				} else if (cut) {
					return false;
				} else {
					// prepare the execution of the next clause
					currentGoal = 0
					currentClause += 1
				}
			}
			case 2 => return c2() // it is the last clause
			 /* {
				if (c2()) {
					return true
				} else {
					// currentClause += 1
					return false
				}
			} */
		}
		next()
	}
	
	// not_attack([],_,_) :- !.
	final def c1() : Boolean = {
		currentGoal match {
			// 0 is the unification of the arguments and terms...
			case 0 => {	
				a1State = a1.manifestState
				if (a1 unify StringAtom.emptyList) {
					currentGoal += 1
				} else {
					a1 setState a1State
					return false
				}
			}
			// special case: the last goal and all previous goals are semi-deterministic goals
			// Hence, we do not have to do anything special w.r.t. to the cut operator...
			case 1 => {	
				cut = true
				currentGoal += 1
				return true
			}
			case 2 => {
				// we have to undo all bindings
				a1 setState a1State
				return false
			}
		}
		c1()
	}
	
	// not_attack([Y|Ys],X,N) :-
	// 	X =\= Y+N, X =\= Y-N,
	// 	N1 is N+1,
	// 	not_attack(Ys,X,N1).	
	final def c2() : Boolean = {

		currentGoal match {
			case 0 => {
				if (a1State == null) a1State = a1.manifestState
				Y = new Variable
				Ys = new Variable
				if (a1 unify new ListElement2(Y,Ys)) {
					currentGoal += 1
				} else {
					a1 setState a1State
					return false
				}
			}
			case 1 => { // Preparation step
				a2State = a2.manifestState
				a3State = a3.manifestState
				currentGoal += 1
			}
			case 2 => {
				if (a2.eval != (Y.eval + a3.eval) && 
						a2.eval != (Y.eval - a3.eval)) {
					currentGoal += 1
				} else {
					// all previous goals are (semi-) deterministic goals
					a1 setState a1State
					a2 setState a2State
					return false
				}
			}
			case 3 => { // Preparation step
				N1 = new Variable
				currentGoal += 1
			}
			case 4 => {
				if (Is2.is(N1,a3.eval + 1)){
					currentGoal += 1
				} else {
					// all previous goals are (semi-) deterministic goals
					// all arguments need to be reset
					currentGoal = 7
				}
			}
			case 5 => { // Preparation step
				goalStack = not_attack3O1(Ys,a2,N1) :: goalStack
				currentGoal += 1
			}
			case 6 => {
				if (goalStack.head.next) {
					return true // the last goal
				} else {
					// all previous goals are (semi-) deterministic goals
					currentGoal += 1
				}
			}
			case 7 => {
				a1 setState a1State
				a2 setState a2State
				a3 setState a3State
				return false
			}
		}
		c2()
	}
	
}



