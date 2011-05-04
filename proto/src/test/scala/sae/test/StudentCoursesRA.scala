package sae.test

import sae.core._
import sae.core.impl._
import sae.core.RelationalAlgebraSyntax._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

/**
 * Test the basic functions of the framework with an example database of students and courses.
 * These tests directly use the relational algebra (RA) operators.
 */
@RunWith(classOf[JUnitRunner]) 
class StudentCoursesRAFunSuite
	 extends FunSuite
{

	val database = new StudentCoursesDatabase()
	import database._

	test("selection") {
		assert( 2 === students.size )
	
		// johnsData(StudentId, john) :- student(StudentId, john)
		val johnsData : Relation[Student] = σ( _.Name ==  "john", students)
		
		assert( 1 === johnsData.size )
		assert( Some(john) === johnsData.uniqueValue )
	}
	
	test("add to materialized selection") {
		// johnsData(StudentId, john) :- student(StudentId, john)
		
		val students = database.students.copy // make a local copy
		
		val johnsData : Relation[Student] = σ( _.Name ==  "john", students) 
		assert( 1 === johnsData.size )
		assert( Some(john) === johnsData.uniqueValue )
		
		val otherJohn = Student(11111, "john")
		students += otherJohn
		
		assert( 2 === johnsData.size )
		val twoJohns = johnsData.asList
		assert( twoJohns.contains(john) )
		assert( twoJohns.contains(otherJohn) )
	}
	
	test("projection") {
		// names(Name) :- student(_, Name)
		val names : Relation[String] = Π[Student, String](_.Name, students)
		
		// the type inference is not strong enough, either we need to supply function argument types of the whole function or of the lambda expression (see below) 
		// val names : Relation[String] = Π( (s:Student) => (s.Name), students)
		

		assert( students.size === names.size)
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
		val students =  database.students.copy
		def fun(x:Student) = x.Name
		val names : Relation[String] = Π[Student, String](fun, Π[Student, Student]( s => s ,students))
				
		val optimized = ExecutionPlan(names)

		assert( optimized ne names)
		
		val directNames : Relation[String] = Π[Student, String](fun, students)
		assert( !(optimized match { case Π(_, Π(_,_)) => true; case _ => false }) )
	}
	
	test("cross product") {
		// println("test cross product")
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
	
	
	test("joins")
	{
		// println("test joins")
		val course_for_student = students ⋈ ( (t:(Student,Enrollment)) => t match {case (s,e) => s.Id == e.StudentId}, enrollments) 
		
		val eise_students = σ( (e:(Student,Enrollment)) => e._2.CourseId == eise.Id, course_for_student)
		
		val sed_students = σ( (e:(Student,Enrollment)) => e._2.CourseId == sed.Id, course_for_student)
		
		// john and sally are registered for eise
		// sally is registered for sed
		
		assert( course_for_student.size === 3 )
		
		val eiseResultList = eise_students.asList
		
		assert( eise_students.size === 2 )
		students.foreach(student =>
			{
				assert( eiseResultList.exists( {case(s,e) => s == student } ) )
			}
		)
		assert( sed_students.asList.exists( {case(s,e) => s == sally } ) )
	}
	
	test("consecutive joins")
	{
		// println("test consecutive joins")

 		val course_for_student = students ⋈ ( (t:(Student,Enrollment)) => t match {case (s,e) => s.Id == e.StudentId}, enrollments) ⋈ ( (t: ((Student,Enrollment), Course)) => t._1._2.CourseId == t._2.Id, courses)
		
		val eise_students = σ( (e:((Student,Enrollment), Course)) => e._2.Id == eise.Id, course_for_student)
		
		val sed_students = σ( (e:((Student,Enrollment), Course)) => e._2.Id == sed.Id, course_for_student)
		
		// john and sally are registered for eise
		// sally is registered for sed
		
		// course_for_student.foreach(println)
		
		assert( course_for_student.size === 3 )
		
		val eiseResultList = eise_students.asList
		
		assert( eise_students.size === 2 )
		students.foreach(student =>
			{
				assert( eiseResultList.exists( {case((s,e),c) => s == student } ) )
			}
		)
		assert( sed_students.asList.exists( {case((s,e),c) => s == sally } ) )

	}
}