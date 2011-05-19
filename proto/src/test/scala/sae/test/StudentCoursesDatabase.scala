package sae
package test

import sae.collections._

/**
 * Defines the data for the student courses example
 */
class StudentCoursesDatabase {
    case class Student(
        val Id : Integer,
        val Name : String)

    case class Course(
        val Id : Integer,
        val Name : String)

    case class Enrollment(
        val StudentId : Integer,
        val CourseId : Integer)

    // student(12345, john).
    // student(12346, sally).
    val john = Student(12345, "john")
    val sally = Student(12346, "sally")

    // student(StudentId, Name)
    object students
            extends Table[Student] {

        val Name : Student => String = s => s.Name

        val Id : Student => Integer = s => s.Id

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

    object courses
            extends Table[Course] {
        
        val Name : Course => String = s => s.Name

        val Id : Course => Integer = s => s.Id
    }

    courses += eise
    courses += sed

    object enrollments
            extends Table[Enrollment] {
        val StudentId : Enrollment => Integer = e => e.StudentId

        val CourseId : Enrollment => Integer = e => e.CourseId
    }

    enrollments += Enrollment(john.Id, eise.Id)
    enrollments += Enrollment(sally.Id, eise.Id)
    enrollments += Enrollment(sally.Id, sed.Id)

}