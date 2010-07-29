package queens

import saere._
import saere.term._
import saere.meta._
import saere.IntegerAtom.IntegerAtom
//import saere.StringAtom.StringAtom 
import saere.StringAtom._

object Demo {

	private def enabled() = {
		println("Assertions are enabled.")
		true
	}
	
	def main(args : Array[String]) : Unit = { 
		
		assert(enabled())
		
		print("Warming up...(may take up to 20seconds)")
		val Qs = new Variable
		val R = new Variable
		val solutions = new MultipleGoals{
			
			def goalCount() = 2

			def goal(i : Int) = i match {
				case 1 => range3(IntegerAtom(1),IntegerAtom(28),R)
				case 2 => queens3O1TC(R,emptyList,Qs)
			}
		}
		val start = System.nanoTime
		var i = 0; while (i < 2) { solutions.next ; i += 1	}
		val duration = System.nanoTime-start
		println(" in "+(duration/1000.0/1000.0/1000.0))
		
		
		/*
	   
	    // RANGE/3
	  {
	 	 val X = new Variable
	 	 val solutions = range3.unify(IntegerAtom(1),IntegerAtom(10),X)
	 	 var solution = 1
	 	  while (solutions.next && solution < 20) {
	 		  println(solution + ": "+X)
	 		  solution += 1
	 	  } 
	  }
	
		// SELECT/3
		{ // select(Ns,Qs,Q).
			val t = ListElement2(IntegerAtom(1),ListElement2(IntegerAtom(2),ListElement2(IntegerAtom(3),ListElement2(IntegerAtom(4),ListElement2(IntegerAtom(5),ListElement2(IntegerAtom(6),StringAtom.emptyList ) ) ) ) ))
			val Qs = new Variable
			val Q = new Variable
			val solutions = select3.unify(t,Qs,Q)
			while (solutions.next) {
				println("Qs="+Qs+", Q="+Q)
			}
		}
		
		// QUEENS/3
		
	
		{ // queens(Ns,[],R).
			
			for (N <- 10 to 22) {
				print("Queens N="+N)
				val Qs = new Variable
				val R = new Variable
				val solutions = new MultipleGoals{
				
					def goalCount = 2
				
					def goal(i : Int) = i match {
						case 1 => range3.unify(IntegerAtom(1),IntegerAtom(N),R)
						case 2 => queens3.unify(R,StringAtom.emptyList,Qs)
					}
				}
				val start = System.nanoTime
				if (solutions.next) {
					print(" => "+Qs)
				}
				val duration = System.nanoTime-start
				println(" in "+(duration/1000.0/1000.0/1000.0))
			}
		}
		
		
		// QUEENS/3(Optimized)
		{ // queens(Ns,[],R).

			for (N <- 10 to 22) {
				print("Queens N="+N)
				val Qs = new Variable
				val R = new Variable
				val solutions = new MultipleGoals{

					def goalCount = 2

					def goal(i : Int) = i match {
						case 1 => range3.unify(IntegerAtom(1),IntegerAtom(N),R)
						case 2 => queens3O1.unify(R,StringAtom.emptyList,Qs)
					}
				}
				val start = System.nanoTime
				if (solutions.next) {
					print(" => "+Qs)
				}
				val duration = System.nanoTime-start
				println(" in "+(duration/1000.0/1000.0/1000.0))
			}
		}
		*/
		
		// QUEENS/3(Optimized - TC)
		{ // queens(Ns,[],R).

			for (N <- 10 to 22) {
				print("Queens N="+N)
				val Qs = new Variable
				val R = new Variable
				val solutions = new MultipleGoals{

					def goalCount() = 2

					def goal(i : Int) = i match {
						case 1 => range3(IntegerAtom(1),IntegerAtom(N),R)
						case 2 => queens3O1TC(R,emptyList,Qs)
					}
				}
				val start = System.nanoTime
				if (solutions.next) {
					print(" => "+Qs)
				}
				val duration = System.nanoTime-start
				println(" in "+(duration/1000.0/1000.0/1000.0))
			}
		}	
				// QUEENS/3(Optimized - TC)
		{ // queens(Ns,[],R).

			
				print("Queens N=28")
				val Qs = new Variable
				val R = new Variable
				val solutions = new MultipleGoals{

					def goalCount() = 2

					def goal(i : Int) = i match {
						case 1 => range3(IntegerAtom(1),IntegerAtom(28),R)
						case 2 => queens3O1TC(R,emptyList,Qs)
					}
				}
				val start = System.nanoTime
				if (solutions.next) {
					print(" => "+Qs)
				}
				val duration = System.nanoTime-start
				println(" in "+(duration/1000.0/1000.0/1000.0))
			
		}	
		
		
	}
}
