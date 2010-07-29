package ancestor

import saere._

import saere.predicate._
import saere.term._
import saere.meta._


/*
 * <p>
 * <b>Original</b>
 * lookup(Key,[(Key,Value)|Dict],Value).
 * lookup(Key,[(Key1,Value1)|Dict],Value) :- Key \= Key1, lookup(Key,Dict,Value).
 * <p>
 * <p>
 * <b>After Transformation</b>
 * 	lookup(Key,A2,Value):- A2=[(Key,Value)|Dict].
 * 	lookup(Key,a2,Value) :-
 * 		a2 = [(Key1,Value1)|Dict],
 * 		Key \= Key1, lookup(Key,Dict,Value).
 * </p>
 */
object lookup3 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
	
		new lookup3p(a1,a2,a3)
	}
}

class lookup3p (private val a1 : Term,private val a2 : Term, val a3:Term) extends MultipleRules { 
		

	// lookup(A1,A2,A3):- A2=[(A1,A3)|_].
	private class lookup3c1 extends OneGoal {
	
		val g1t = new ListElement2(new And2(a1,a3),new Variable) 
		
		def goal = Unify2.apply(a2, g1t)
		
	}
	//  lookup(A1,A2,A3) :-
	//		A2 = [(Key1,_)|Dict],
	//		A1 \= Key1,
	//		lookup(A1,Dict,A3).
	//  <=> lookup(A1,A2,A3) :- A2 = [(Key1,_)|Dict], A1 \= Key1, lookup(A1,Dict,A3).
	private class lookup3c2 extends MultipleGoals {
	
		val g1v1 = new Variable // Key1
		val g1v2 = new Variable // Dict
		val g1t1 = new ListElement2(new And2(g1v1,new Variable),g1v2)
		
		def goalCount() : Int = 3
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a2, g1t1)
			case 2 => NotUnify2.apply(a1, g1v1)
			case 3 => lookup3(a1,g1v2,a3)
		}	
	}
		
	val ruleCount = 2
	
	def rule(i : Int ) = i match {
		case 1 => new lookup3c1
		case 2 => new lookup3c2 
	}
}



