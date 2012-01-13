package sae

import syntax.RelationalAlgebraSyntax._
import test.StudentCoursesDatabase
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * Author: Ralf Mitschke
 * Date: 13.01.12
 * Time: 10:48
 *
 */
class TestObserverManipulation extends ShouldMatchers
{

    @Test
    def testSelectionRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        import o._

        val selection = σ((_: Student).Name == "Mark")(students)
        selection.addObserver(o);
        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark
        o.events should be(List(AddEvent(mark)))

        selection.clearObserversForChildren(_ != students)

        // check that the the observer was correctly removed
        students.observers should have size (0)
    }

    @Test
    def testProjectionRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[String]
        import o._

        val projection = Π((_: Student).Name)(students)
        projection.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark

        o.events should be(List(AddEvent("Mark")))

        projection.clearObserversForChildren(_ != students)

        // check that the the observer was correctly removed
        students.observers should have size (0)
    }

    @Test
    def testCrossProductRemoval() {
        val database = new StudentCoursesDatabase()

        import database._
        val o = new MockObserver[(Student, Enrollment)]
        import o._

        val join = students × enrollments
        join.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark

        o.events should be(
            List(
                AddEvent((mark, Enrollment(12346, 51))),
                AddEvent((mark, Enrollment(12345, 23))),
                AddEvent((mark, Enrollment(12346, 23)))
            )
        )

        students -= mark
        o.clearEvents()
        enrollments += new Enrollment(1, 1)

        o.events should be(
            List(
                AddEvent((john, Enrollment(1, 1))),
                AddEvent((sally, Enrollment(1, 1)))
            )
        )

        join.clearObserversForChildren((o: Observable[_ <: AnyRef]) => (o != students && o != enrollments))

        // check that the the observer was correctly removed
        students.observers should have size (0)
        enrollments.observers should have size (0)
    }

    @Test
    def testJoinRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[(Student, Enrollment)]
        import o._

        val join = ((students, students.Id) ⋈(enrollments.StudentId, enrollments)) {(s: Student,
                                                                                     e: Enrollment) =>
            (s, e)
        }
        join.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark

        val enrollment = new Enrollment(mark.Id, 2080)
        enrollments += enrollment

        o.events should be(List(AddEvent((mark, enrollment))))

        join.clearObserversForChildren((o: Observable[_ <: AnyRef]) => (o != students && o != enrollments))

        // check that the the observer was correctly removed
        students.observers should have size (0)
        enrollments.observers should have size (0)
    }

    @Test
    def testJoinMultipleTablesRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[(Student, Course)]
        import o._

        val join =
            (
                    (
                            (
                                    (
                                            students,
                                            students.Id
                                            ) ⋈(
                                            enrollments.StudentId,
                                            enrollments
                                            )
                                    ) {
                                (s: Student, e: Enrollment) => (s, e)
                            },
                            (se: (Student, Enrollment)) => (se._2.CourseId)
                            ) ⋈(
                            (_: Course).Id,
                            courses
                            )
                    ) {
                (se: (Student, Enrollment), c: Course) => (se._1, c)
            }

        join.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark

        val gdi1 = new Course(2080, "GDI I")

        courses += gdi1

        val enrollment = new Enrollment(mark.Id, gdi1.Id)
        enrollments += enrollment

        o.events should be(List(AddEvent((mark, gdi1))))

        join.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != students &&
                        o != enrollments &&
                        o != courses
            }
        )

        // check that the the observer was correctly removed
        students.observers should have size (0)
        enrollments.observers should have size (0)
        courses.observers should have size (0)
    }

    @Test
    def testDistinctRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        import o._

        val distinct = δ(students)
        distinct.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark
        students += mark

        o.events should be(List(AddEvent(mark)))

        distinct.clearObserversForChildren(_ != students)

        // check that the the observer was correctly removed
        students.observers should have size (0)
    }

}