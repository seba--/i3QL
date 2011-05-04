package sae.test

import sae.core._
import sae.core.impl._
import sae.core.RelationalAlgebraSyntax._


/**
 * Defines the data for the student courses example
 */
class StudentCoursesDatabase
{
	case class Student
	(
		val Id : Int,
		val Name : String
	)
	
	case class Course
	(
		val Id : Int,
		val Name : String
	)
	
	case class Enrollment
	(
		val StudentId : Int,
		val CourseId : Int
	)
	

	// student(12345, john).
	// student(12346, sally).
	val john = Student(12345, "john")
	val sally = Student(12346, "sally") 

	// student(StudentId, Name)
	object students
	 extends Relation[Student]
		with MultisetRelation[Student]
	{
	
		def materialize() : Unit = { /* nothing to do, the set itself is the data */ }
		
		def Name : Student => String = s => s.Name
		
		def Id : Student => Int = s => s.Id
		
		/*
		// careful, if the outer database is declared as object the initialization must be inlined
		// there is no guarantee that the outer object is initialized before the inner object
		this += john
		this += sally
		*/
	}

	students += john
	students += sally


	val eise = Course(23, "EiSE") 
	val sed = Course(51, "SE-D&C")
	




	// course(CourseId, Name)
	def courses : MaterializedRelation[Course] = 
	{
		val courses = new MultisetRelation[Course]{ def materialize() : Unit = { /* nothing to do, the set itself is the data */ } }  
		courses += eise
		courses += sed 
	}
	
	// enrollment(StudentId, CourseId)
	def enrollments : MaterializedRelation[Enrollment] = 
	{ 
		val enrollments = new MultisetRelation[Enrollment]{ def materialize() : Unit = { /* nothing to do, the set itself is the data */ } }
		enrollments += Enrollment(john.Id, eise.Id)
		enrollments += Enrollment(sally.Id, eise.Id)
		enrollments += Enrollment(sally.Id, sed.Id)
		enrollments 
	}
}