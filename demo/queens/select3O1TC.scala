package queens

import saere._

import saere.op._
import saere.term._


object select3O1TC {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
	
		new select3O1TCp(a1,a2,a3)
	}
}

final class select3O1TCp (
		private var a1 : Term,
		private var a2 : Term,
		private var a3 : Term
) extends Solutions { 
		
	// select([X|Xs],Xs,X).
	// select([X|Xs],[X|Zs],R) :- select(Xs,Zs,R).
	// <=> select(A1,A2,R) :- A1=[X|Xs], A2=[X|Zs], select(Xs,Zs,R).
	
	private var a1State : State = null
	private var a2State : State = null
	private var a3State : State = null

	private var X : Term = null
	private var Xs : Term = null
	private var Zs : Variable = null
//	private var R : Variable = null

	private var currentClause = 1
	private var currentGoal = 0 // i.e., "no initialization has been done"

//	private var goalStack : List[Solutions] = Nil

	def next() : Boolean = {
		
		currentClause match {
			case 1 => {
				if(c1()) {
					return true
				} else {
					currentGoal = 0
					currentClause += 1
				}
			}
			case 2 => {
				if(c2()) {
					// prepare recursive call
					a1State = null
					a2State = null
					a3State = null
					X = null
					Xs = null
					Zs = null
		//			R = null					
					currentClause = 1
					currentGoal = 0
		//			goalStack = Nil
				} else {
					return false
				}
			}
		}
		next()
	}


	// select([X|Xs],Xs,X).
	// <=> select(A1,Xs,X) :- A1=[X|Xs].
	final def c1() : Boolean = {
		// println("select3o1 - c1 called: "+currentGoal)
		currentGoal match {
			case 0 => {
				a1State = a1.manifestState
				a2State = a2.manifestState
				a3State = a3.manifestState
				if (a1 unify new ListElement2(a3,a2)) {				
					currentGoal += 1
					return true // last goal
				} else {
					currentGoal += 1
				}
			}
			case 1 => {
				a1 setState a1State
				a2 setState a2State
				a3 setState a3State
				return false
			}
		}
		c1()
	}

		
		
	final def c2() : Boolean = {
		//println("select3o1TC - c2 called: "+currentGoal)
			// select(A1,A2,R) :- A1=[X|Xs], A2=[X|Zs], select(Xs,Zs,R).
		if (a1State == null) a1State = a1.manifestState
		X = new Variable
		Xs = new Variable
		if (!(a1 unify new ListElement2(X,Xs))) {
			a1 setState a1State
			return false
		}
				
		if (a2State == null) a2State = a2.manifestState
		Zs = new Variable
		if (!(a2 unify new ListElement2(X,Zs))) {
			a1 setState a1State
			a2 setState a2State
			return false
		}
				
		// Prepare Tail Call
		a1 = Xs
		a2 = Zs
		return true
	}
			
	final def c2K() : Boolean = {
		//println("select3o1 - c2 called: "+currentGoal)
		currentGoal match {
			case 0 => { // select(A1,A2,R) :- A1=[X|Xs], A2=[X|Zs], select(Xs,Zs,R).
				if (a1State == null) a1State = a1.manifestState
				X = new Variable
				Xs = new Variable
				if (a1 unify new ListElement2(X,Xs)) {
					currentGoal += 1
				} else {
					a1 setState a1State
					return false
				}
			}
			case 1 => { 
				if (a2State == null) a2State = a2.manifestState
				Zs = new Variable
				if (a2 unify new ListElement2(X,Zs)) {
					currentGoal += 1
				} else {
					a1 setState a1State
					a2 setState a2State
					return false
				}
			}	
			case 2 => {
				// Prepare Tail Call
				a1 = Xs
				a2 = Zs
				return true
			}
		}
		c2K()
	}
}



