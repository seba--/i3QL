package sae.test

import sae.core._
import sae.core.impl._
import sae.core.SQLSyntax._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite


/**
 * Test the sql syntax on the student courses example
 */
@RunWith(classOf[JUnitRunner]) 
class StudentCoursesSQLFunSuite
	 extends FunSuite  
		with StudentCoursesDatabase
{
	test("select") {
		val names = select ((s:Student) => s.Name) from students

		assert( names.size === students.size)
		val nameList = names.asList
		students.foreach( s =>
			{
				nameList.contains(s.Name)
			}	
		)
	}
	
	test("select distinct") {
		val students = this.students // make a local copy of students for this test
		val names = select distinct ((s:Student) => s.Name) from students
		
		assert( names.size === 2)
		
		val otherJohn = Student(11111, "john")
		students += otherJohn
		
		assert( names.size === 2)
		val nameList = names.asList
		students.foreach( s =>
			{
				nameList.contains(s.Name)
			}	
		)
		
		val distinctStudents = select distinct * from students
		assert( students.size === distinctStudents.size)
	}

	test("where") {
		// * is a shortcut that really omits the projection function
		val johnsData  = select (*) from students where (_.Name ==  "john")
		
		// val johnsData  = select ((x:Student) => x) from students where (_.Name ==  "john")
		assert( johnsData.size === 1 )
		assert( johnsData.uniqueValue === Some(john) )
	}

}