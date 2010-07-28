package ancestor

import saere._
import saere.predicate._
import saere.meta._
import saere.StringAtom.StringAtom

object father2 {
	
	def unify(a1 : Term, a2 : Term) : Solutions = {
	
		new father2pSolutions(a1,a2)
		
	}
	
}


class father2pSolutions(private val a1 : Term, val a2 : Term) extends MultipleRules { 
		
	/*
		private val a1State : State = a1.manifestState 
		private val a2State : State = a2.manifestState 
	*/
	
	// father(X,Y) :- X = 'Reinhard', Y = 'Alice' 
	private class father2c1 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Reinhard")
		val g2a2 : Term = StringAtom("Alice")
			
		val goalCount : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2(a1, g1a2)
			case 2 => Unify2(a2, g2a2)
		}	
	}
	
	// father(X,Y) :- X = 'Reinhard', Y = 'Thilo' 
	// father(X,Y) :- = (X,'Reinhard'), = (Y,'Thilo') 
	private class father2c2 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Reinhard")
		val g2a2 : Term = StringAtom("Thilo")
	
		val goalCount : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2(a1, g1a2)
			case 2 => Unify2(a2,g2a2)
		}		
	}
	
	// THE FOLLOWING DEMONSTRATES THE EFFECT OF INLINING "MULTIPLE_GOALS" RESOLUTION
	private class father2c3Solutions extends Solutions {
	
		val g1 : Term = StringAtom("Werner")
		val g2 : Term = StringAtom("Michael")
	
		var activeGoal = 1
		
		var solutionsG1 = Unify2(a1, g1)
		var solutionsG2 : Solutions = null
		
		def next() : Boolean = {
			while (activeGoal > 0) {
				activeGoal match {
					case 1 =>
						if (solutionsG1.next){
							activeGoal += 1
							solutionsG2 = Unify2(a2,g2)
						} else {
							solutionsG1 = null
							activeGoal -= 1
						}
					case 2 =>
						if (solutionsG2.next) {
							return true
						}
						else {
							solutionsG2 = null // is this required?
							activeGoal -= 1
						}
				}
			}
			false
		}		
	}	
	
	def ruleCount = 3
	
	def rule(i : Int) = i match {
		case 1 => new father2c1
		case 2 => new father2c2
		case 3 => new father2c3Solutions 
	}
	
}



