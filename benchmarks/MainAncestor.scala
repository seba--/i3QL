package ancestor

import saere._
import saere.term._
import saere.meta._
import saere.StringAtom.StringAtom

object Demo {
	
	def main(args : Array[String]) : Unit = { 
	  
		// BASIC UNIFICATION
		{ 	
			print("\"true\" expected - result "); println(StringAtom("Maria") unify StringAtom("Maria"))

			print("\"true\" expected - result "); println((new Variable) unify (new Variable))

			val V = new Variable
			print("\"true\" expected - result "); println(V unify V)

			print("\"false\" expected - result "); println(StringAtom("Maria") unify StringAtom("Alice"))

			val X = new Variable
			print("\"true\" expected - result "); println(StringAtom("Maria") unify X)
			val Y = new Variable
			print("\"true\" expected - result "); println(Y unify StringAtom("Maria"))
			print("\"true\" expected - result "); println(X unify Y)
		}
		
		// VARIABLES THAT SHARE
		{
			//X = Y,A = B, Y = A, Y = a. => X = a, Y = a, A = a, B = y
			val X = new Variable
			val Y = new Variable
			val A = new Variable
			val B = new Variable
			X unify Y
			A unify B
			val YState = Y.manifestState
			val AState = A.manifestState
			Y unify A
			Y unify StringAtom("a")
			println("\"a a a a\" expected - result "+X+" "+Y+" "+A+" "+B)
			Y setState YState
			A setState AState
			Y unify StringAtom("b")
			A unify StringAtom("c")
			println("\"b b c c\" expected - result "+X+" "+Y+" "+A+" "+B)
		}


	  // MALE/1
	  {
	 	  val solutions = male1.unify(StringAtom("Thilo"))
		  print("\"true\" expected - result "); println(solutions.next)
		  print("\"false\" expected - result "); println(solutions.next)
	  }
	   
	  print("\"false\" expected - result "); println(male1.unify(StringAtom("Maria")).next) 
	  
	  { 
	 	  val X = new Variable 
	 	  val solutions = male1.unify(X)
	 	  print("\"Thilo Michael Werner Reinhard\" expected - result "); 
	 	  while (solutions.next) {
	 		  print(X) ; print(" ")
	 	  }
	 	  println
	  }
	   
	   // FATHER/2
	   { 
	  	  val solutions = father2.unify(StringAtom("Reinhard"),StringAtom("Werner"))
	 	  print("\"false\" expected - result ") ; print (solutions.next)
	 	  println
	  } 
	   { 
	  	  val solutions = father2.unify(StringAtom("Werner"),StringAtom("Michael"))
	 	  print("\"true\" expected - result ") ; print (solutions.next)
	 	  println
	  } 

	   { 
	 	  val X = new Variable
	 	  val Y = new Variable
	 	  val solutions = father2.unify(X,Y)
	 	  print("\"(Reinhard,Alice) (Reinhard,Thilo) (Werner,Michael)\" expected - result "); 
	 	  while (solutions.next) {
	 		  print("("+X+","+Y+") ")
	 	  }
	 	  println
	  }
	  { 
	 	  val Y = new Variable
	 	  val solutions = father2.unify(StringAtom("Reinhard"),Y)
	 	  print("\"Alice Thilo\" expected - result "); 
	 	  while (solutions.next) {
	 		  print(Y+" ")
	 	  }
	 	  println
	  }
	   
	   // MOTHER/2
	  { 
	 	  val Y = new Variable
	 	  val solutions = mother2.unify(Y,StringAtom("Leonie"))
	 	  print("\"Heidi\" expected - result "); 
	 	  while (solutions.next) {
	 		  print(Y+" ")
	 	  }
	 	  println
	  }
	   
	  // PARENT/2
	  { 
	 	  val X = new Variable
	 	  val Y = new Variable
	 	  val solutions = parent2.unify(X,Y)
	 	  print("\"(Christel,Michael) (Heidi,Valerie) (Heidi,Leonie) (Magdalena,Alice) (Magdalena,Thilo) (Reinhard,Alice) (Reinhard,Thilo) (Werner,Michael)\" expected - result "); 
	 	  while (solutions.next) {
	 		  print("("+X+","+Y+") ")
	 	  }
	 	  println
	  }
	   { 
	 	  val X = new Variable
	 	  val solutions = parent2.unify(X,StringAtom("Michael"))
	 	  print("\"Christel Werner\" expected - result "); 
	 	  while (solutions.next) {
	 		  print(X+" ")
	 	  }
	 	  println
	  }
	   
	   // SIBLING/2
	   { 
	 	  val X = new Variable
	 	  val Y = new Variable
	 	  val solutions = sibling2.unify(X,Y)
	 	  print("\"(Alice Thilo) (Thilo Alice)\" expected - result "); 
	 	  while (solutions.next) {
	 		  print("("+X+","+Y+") ")
	 	  }
	 	  println
	  }
	     
	  // LOOKUP/3
	  {
	 	 // lookup(k1,Dict,v1).
	 	 val Dict = new Variable
	 	 val solutions = lookup3(StringAtom("k1"),Dict,StringAtom("v1"))
	 	  while (solutions.next) {
	 		  print(Dict)
	 	  }
	 	  println
	  }
	   {
	  	    val Dict = new Variable
	 	 val solutions = new MultipleGoals{
	 		 
	 	 	  def goalCount = 2
	 	 	  
	 	 	  def goal(i : Int) = 
	 	 	 	  if (i == 1) 
	 	 	 	 	  lookup3(StringAtom("k1"),Dict,StringAtom("v1"))
	 	 	 	 else
	 	 	 		  lookup3(StringAtom("k2"),Dict,StringAtom("v2"))
	 	 	 		
	 	  }
	 	 while (solutions.next) {
	 		 print(Dict)
	 	 }
	 	 println
 	  }
	  {
	 	   val Dict = new Variable
	 	   val Value = new Variable
	 	 val solutions = new MultipleGoals{
	 		 
	 	 	  def goalCount = 4
	 	 	  
	 	 	  def goal(i : Int) = 
	 	 	 	  if (i == 1) 
	 	 	 	 	  lookup3(StringAtom("k1"),Dict,StringAtom("v1"))
	 	 	 	 else if (i == 2)
	 	 	 		  lookup3(StringAtom("k2"),Dict,StringAtom("v2"))
	 	 	 	 else if (i == 3)
	 	 	 		  lookup3(StringAtom("k3"),Dict,StringAtom("v5"))	  
	 	 	     else 
	 	 	 		  lookup3(StringAtom("k2"),Dict,Value)
	 	 	 		
	 	  }
	 	 while (solutions.next) {
	 		 print(Value)
	 	 }
	 	 println
 	  }
	   
	 
	}
}
