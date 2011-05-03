package sae.test

import sae.core._
import sae.core.impl._
import sae.core.RelationalAlgebraSyntax._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

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
	
/**
 * Defines the data for the student courses example
 */
trait StudentCoursesDatabase
{
	// student(12345, john).
	// student(12346, sally).
	val john = Student(12345, "john")
	val sally = Student(12346, "sally") 

	val eise = Course(23, "EiSE") 
	val sed = Course(51, "SE-D&C")

	// student(StudentId, Name)
	def students : MaterializedRelation[Student] = 
	{
		val students = new MultisetRelation[Student]{ def materialize() : Unit = { /* nothing to do, the set itself is the data */ } } 
		students += john
		students += sally
	}

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
		enrollments 
	}
}