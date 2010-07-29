package ancestor

import saere._
import saere.predicate._
import saere.meta._
import saere.StringAtom.StringAtom

object mother2 {
	
	def unify(a1 : Term, a2 : Term) : Solutions = {
	
		new mother2p(a1,a2)
		
	}
	
}


class mother2p(private val a1 : Term, val a2 : Term) extends MultipleRules { 
		
	private val a1State : State = a1.manifestState 
	private val a2State : State = a2.manifestState 
	
	private class mother2c1 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Christel")
		val g2a2 : Term = StringAtom("Michael")
			
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1, g1a2)
			case 2 => Unify2.apply(a2, g2a2)
		}	
	}

	private class mother2c2 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Heidi")
		val g2a2 : Term = StringAtom("Valerie")
	
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1, g1a2)
			case 2 => Unify2.apply(a2, g2a2)
		}		
	}
	
	private class mother2c3 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Heidi")
		val g2a2 : Term = StringAtom("Leonie")
	
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1, g1a2)
			case 2 => Unify2.apply(a2, g2a2)
		}		
	}
	private class mother2c4 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Magdalena")
		val g2a2 : Term = StringAtom("Alice")
	
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1, g1a2)
			case 2 => Unify2.apply(a2, g2a2)
		}		
	}
	private class mother2c5 extends MultipleGoals {
	
		val g1a2 : Term = StringAtom("Magdalena")
		val g2a2 : Term = StringAtom("Thilo")
	
		def goalCount() : Int = 2
		
		def goal(i : Int ) : Solutions = i match {
			case 1 => Unify2.apply(a1, g1a2)
			case 2 => Unify2.apply(a2, g2a2)
		}		
	}
	
	val ruleCount = 5
	
	def rule(i : Int) = i match {
		case 1 => new mother2c1
		case 2 => new mother2c2
		case 3 => new mother2c3
		case 4 => new mother2c4
		case 5 => new mother2c5
	}
	
}



