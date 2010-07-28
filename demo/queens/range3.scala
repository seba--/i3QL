package queens

import saere._

import saere.predicate._
import saere.term._
import saere.meta._
import saere.IntegerAtom.IntegerAtom
import saere.StringAtom.StringAtom 


/*
 * <p>
 * <b>Original</b>
 * lookup(Key,[(Key,Value)|Dict],Value).
 * lookup(Key,[(Key1,Value1)|Dict],Value) :- Key \= Key1, lookup(Key,Dict,Value).
 * <p>
 * <p>
 * <b>Structure of the clauses after transformation:</b>
 * 	lookup(Key,A2,Value):- A2=[(Key,Value)|Dict].
 * 	lookup(Key,a2,Value) :-
 * 		a2 = [(Key1,Value1)|Dict],
 * 		Key \= Key1, lookup(Key,Dict,Value).
 * </p>
 */
object range3 {
	
	def apply(a1 : Term, a2:Term, a3:Term) : Solutions = {
	
		new range3p(a1,a2,a3)
	}
}

class range3p (private val a1 : Term,private val a2 : Term, val a3:Term) extends MultipleRules { 
		

	// range(A1,A2,A3) :- A2 = A1, A3 = [A1] // TODO Completely support cuts ("!").
	private class range3c1 extends MultipleGoals {
		
		def goalCount = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2(a2, a1)
			case 2 => Unify2(a3, new ListElement2(a1,saere.StringAtom.emptyList) )
		}
	}
	
	// range(A1,A2,A3) :- 
	//		A3 = [A1|Ns], 
	//		A1 < A2, 
	//		M1 is A1+1,
	//		range(M1,A2,Ns).
	// range(A1,A2,A3) :- A3 = [A1|Ns],	A1 < A2, M1 is A1+1, range(M1,A2,Ns).
	private class range3c2 extends MultipleGoals {
	
		val NS = new Variable 
		val M1 = new Variable 
		
		val goalCount : Int = 4
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2(a3, new ListElement2(a1,NS))
			case 2 => new Once{
				def eval() = 
					Smaller2.isSmaller(a1, a2)
			}
			case 3 => Is2.call(M1,new Add2(a1,IntegerAtom(1)))
			case 4 => range3(M1,a2,NS)
		}	
	}
		
	val ruleCount = 2
	
	def rule(i : Int ) = i match {
		case 1 => new range3c1
		case 2 => new range3c2 
	}
}



