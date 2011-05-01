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
	// student(12345, john).
	// student(12346, sally).
	val john = Student(12345, "john")
	val sally = Student(12346, "sally") 

	val eise = Course(23, "EiSE") 
	val sed = Course(51, "SE-D&C")

	// student(StudentId, Name)
	def students : Relation[Student] = 
	{
		val students = new MultisetRelation[Student]{ def arity = 2 } 
		students += john
		students += sally
	}

	// course(CourseId, Name)
	def courses : Relation[Course] = 
	{
		val courses = new MultisetRelation[Course]{ def arity = 2 }  
		courses += eise
		courses += sed 
	}
	
	// enrollment(StudentId, CourseId)
	def enrollments : Relation[Enrollment] = 
	{ 
		val enrollments = new MultisetRelation[Enrollment]{ def arity = 2 }
		enrollments 
	}
	

	
	
	test("selection") {
		// johnsId(StudentId) :- student(StudentId, john)
		val johnsId : Relation[Student] = σ( _.Name ==  "john", students) 
		assert( johnsId.size === 1)
		assert( johnsId.uniqueValue === Some(john))
	}
	
	test("add to materialized selection") {
		// johnsId(StudentId) :- student(StudentId, john)
		var students = this.students
		val johnsId : Relation[Student] = σ( _.Name ==  "john", students) 
		assert( johnsId.size === 1)
		assert( johnsId.uniqueValue === Some(john))
		
		val otherJohn = Student(11111, "john")
		students += otherJohn
		
		assert( johnsId.size === 2)
		val twoJohns = johnsId.asList
		assert( twoJohns.contains(john) )
		assert( twoJohns.contains(otherJohn) )
	}
	
	test("projection") {
		// names(Name) :- student(_, Name)
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
	
	
	test("optimized projection") {
		// names(StudentId) :- student(StudentId, john)
		// there is an inner projection that does nothing
		val students = this.students
		def fun(x:Student) = x.Name
		val names : Relation[String] = Π[Student, String](fun, Π[Student, Student]( s => s ,students))
				
		val optimized = ExecutionPlan(names)

		assert( optimized ne names)
		
		val directNames : Relation[String] = Π[Student, String](fun, students)
		assert( !(optimized match { case Π(_, Π(_,_)) => true; case _ => false }) )
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