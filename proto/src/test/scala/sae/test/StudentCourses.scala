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
 * Test the basic functions of the framework with an example database of students and courses
 */

@RunWith(classOf[JUnitRunner]) 
class StudentCoursesFunSuite
	extends FunSuite  
{

	val students : Relation[Student] = new MultisetRelation[Student]{ def arity = 2 } // student(StudentId, Name)
	val courses : Relation[Course] = new MultisetRelation[Course]{ def arity = 2 }  // course(CourseId, Name)
	val enrollments : Relation[Enrollment] =  new MultisetRelation[Enrollment]{ def arity = 2 } // enrollment(StudentId, CourseId)
	
	// student(12345, john).
	// student(12346, sally).
	val john = Student(12345, "john")
	val sally = Student(12346, "sally") 

	students += john
	students += sally
	
	val eise = Course(23, "EiSE") 
	val sed = Course(51, "SE-D&C")
	courses += eise
	courses += sed 
	
	test("selection") {
		// johnsId(StudentId) :- student(StudentId, john)
		val johnsId : Relation[Student] = σ( _.Name ==  "john", students) 
		assert( johnsId.size === 1)
		assert( johnsId.uniqueValue === Some(john))
	}
	
	test("projection") {
		// johnsId(StudentId) :- student(StudentId, john)
		val names : Relation[String] = Π[Student, String](_.Name, students)
		
		// the type inference is not strong enough, either we need to supply function argument types of the whole function or of the lambda expression (see below) 
		// val names : Relation[String] = Π( (s:Student) => (s.Name), students)
		

		assert( names.size === students.size)
		val nameList = names.asList
		students.foreach( s =>
			{
				nameList.contains(s.Name)
			}	
		)
	}
	
	test("cross product") {
		// student_courses(StudentId, SName, CourseId, CName) :- student(StudentId, SName), course(CourseId, CName).
		// actually what we get looks more like the following  query.
		// student_courses(student(StudentId, SName),course(CourseId, CName)) :- student(StudentId, SName), course(CourseId, CName).
		
		val student_courses = students × courses
		assert( student_courses.size === students.size + courses.size)
		// assert( student_courses.arity === students.arity + courses.arity)
		
		// the cross product should contain every "student course" pair
		val resultList = student_courses.asList
		students.foreach( student => 
			{
				val onestudents_courses = resultList.filter( {case (s,c) => s == student} )
				courses.foreach( course =>
					onestudents_courses.contains(course)
				)
			}
		)
		courses.foreach( course =>
			{
				val onecourses_students = resultList.filter( {case (s,c) => c == course} )
				students.foreach( student => 
					onecourses_students.contains(student)				
				)
			}
		)
	}
}