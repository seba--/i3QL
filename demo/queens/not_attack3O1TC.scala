package queens

import saere._

//import saere.op._
//import saere.op.arith._
import saere.term._
import saere.IntegerAtom.IntegerAtom
import saere.StringAtom._ 



object not_attack3O1TC {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
		new not_attack3O1TCp(a1,a2,a3)
	}
}

private final class not_attack3O1TCp (
	private var a1 : Term,
	private var a2 : Term,
	private var a3 : Term
) extends Solutions { 
	
	// BUG... we do need to store the very original state of a1..a3 in case of 
	// a tail call to make sure we can reset the original arguments and not the 
	// intermediate ones when the tail recursive call fails!
	
	private var a1State : State = null
	private var a2State : State = null
	private var a3State : State = null

	private var Y : Term = null
	private var Ys : Term = null
	private var N1 : IntegerAtom = null

	private var commit = false	
	private var currentClause = if (a1.isCompoundTerm && {val ct = a1.asCompoundTerm; ct.arity == 2 && ct.functor == ListElement2.functor}) 2 else 1
	private var currentGoal = 0 // i.e., "no initialization has been done"

//	private var goalStack : List[Solutions] = Nil

	def commitChoice() = this.commit 
	
	def next() : Boolean = {
		
		currentClause match {
			case 1 => { 
				if (c1()) {
					return true
				} else if (commit) {
					return false;
				} else {
					// reset shared information
					currentGoal = 0
					currentClause += 1
				}
			}
			case 2 => {
				if (c2()) {
					// prepare recursive call
					a1State = null
					a2State = null
					a3State = null
					Y = null
					Ys = null
					N1 = null
//					commit = false
					currentClause = if (a1.isCompoundTerm && {val ct = a1.asCompoundTerm; ct.arity == 2 && ct.functor == ListElement2.functor}) 2 else 1
					currentGoal = 0
//					goalStack = Nil
				} else {
					// currentClause += 1
					return false
				}
			}
		}
		next()
	}
	
	// not_attack([],_,_) :- !.
	private def c1() : Boolean = {
		currentGoal match {
			// 0 is the unification of the arguments and terms...
			case 0 => {	
				a1State = a1.manifestState
				if (a1 unify emptyList) {
					currentGoal += 1
				} else {
					a1 setState a1State
					return false
				}
			}
			// special case: the last goal and all previous goals are semi-deterministic goals
			case 1 => {	
				commit = true
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
	private def c2V() : Boolean = {

//		currentGoal match {
//			case 0 => {
	
				if (a1State == null) a1State = a1.manifestState
				Y = new Variable
				Ys = new Variable
				if (!(a1 unify new ListElement2(Y,Ys))) {
					a1 setState a1State
					return false
				}

				if (!(a2.eval != (Y.eval + a3.eval) && 
						a2.eval != (Y.eval - a3.eval))) {
					// all previous goals are (semi-) deterministic goals
					a1 setState a1State
	//				a2 setState a2State
					return false
				}

				N1 = IntegerAtom(a3.eval + 1)

				a1 = Ys
				//a2 = a2
				a3 = N1
				return true
		//	}
			/*
			case 4 => {
				a1 setState a1State
			//	a2 setState a2State
			//	a3 setState a3State
				return false
			}*/
//		}
//		c2()
	}
	
	
		// not_attack([Y|Ys],X,N) :-
		// 	X =\= Y+N, X =\= Y-N,
		// 	N1 is N+1,
		// 	not_attack(Ys,X,N1).	
		private def c2() : Boolean = {
			
//println(a1 + " --- " + a2 + " --- " + a3 + " --- ")			
			if (a1.arity == 2 && a1.functor == ListElement2.functor) {
					// Y has to be instantiated... because, otherwise the computation will fail!
					Y = a1.arg(0)
					// YS is just passed to another goal (which will manifest the state if required)
					Ys = a1.arg(1)
			} else 
				return c2V()
			
			if (!(a2.eval != (Y.eval + a3.eval) && 
					a2.eval != (Y.eval - a3.eval))) {
				// all previous goals are (semi-) deterministic goals
				return false
			}

			N1 = IntegerAtom(a3.eval + 1)

			a1 = Ys
			a3 = N1
			return true
		}
	
}



