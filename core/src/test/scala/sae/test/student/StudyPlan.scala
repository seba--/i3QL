package sae.test.student

/**
 *
 * @author Ralf Mitschke
 *
 */
case class StudyPlan(department: String)

//case class Semester(plan: StudyPlan, number:Int)

case class StudyCourse(department: String, title: String, semester: Int)

case class CoursePrereqisite(department: String, courseTitle: String, prerequisiteTitle: String)

trait CourseMaterial

case class CourseDescription(title: String)

case class RecommendedBook(course: CourseDescription, author: String, title: String)

case class Lecturer(course: CourseDescription, name: String)