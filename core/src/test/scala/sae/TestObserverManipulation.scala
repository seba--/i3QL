package sae

import functions.{Min, Count}
import syntax.RelationalAlgebraSyntax._
import test.StudentCoursesDatabase
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers
import sae.MockObserver._

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
    def testJoinMultipleIndicesRemoval() {
        val database = new StudentCoursesDatabase()
        import database._

        val joinA = ((students, students.Id) ⋈(enrollments.StudentId, enrollments)) {(s: Student,
                                                                                     e: Enrollment) =>
            s
        }

        val joinB = ((students, students.Name) ⋈(persons.Name, persons)) {(s: Student,
                                                                                      e: Person) =>
            s
        }

        enrollments.observers should have size (1)
        persons.observers should have size (1)
        students.observers should have size (2)

        joinA.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != students && o != enrollments
            }
        )

        enrollments.observers should have size (0)
        persons.observers should have size (1)
        students.observers should have size (1)
    }

    @Test
    def testDistinctRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

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

    @Test
    def testUnionRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

        val studentsA = students.copy
        val studentsB = students.copy

        val union = studentsA ∪ studentsB
        union.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        studentsA += mark
        o.events should be(List(AddEvent(mark)))
        studentsB += mark
        o.events should be(List(AddEvent(mark), AddEvent(mark)))

        union.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != studentsA && o != studentsB
            }
        )

        // check that the the observer was correctly removed
        studentsA.observers should have size (0)
        studentsB.observers should have size (0)
    }

    @Test
    def testIntersectionRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

        val studentsA = students.copy
        val studentsB = students.copy

        val intersection = studentsA ∩ studentsB
        intersection.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        studentsA += mark
        o.events should be(Nil)
        studentsB += mark
        o.events should be(List(AddEvent(mark)))

        intersection.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != studentsA && o != studentsB
            }
        )

        // check that the the observer was correctly removed
        studentsA.observers should have size (0)
        studentsB.observers should have size (0)
    }

    @Test
    def testDifferenceRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

        val studentsA = students.copy
        val studentsB = students.copy

        val difference = studentsA ∖ studentsB
        difference.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        studentsA += mark
        o.events should be(List(AddEvent(mark)))

        studentsB += mark
        o.events should be(List(
            RemoveEvent(mark),
            AddEvent(mark)
        ))

        difference.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != studentsA && o != studentsB
            }
        )

        // check that the the observer was correctly removed
        studentsA.observers should have size (0)
        studentsB.observers should have size (0)
    }

    @Test
    def testTransitiveClosureRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[CoursePrerequisite]
        

        val transitiveClosure = Π(
            (edge: (Integer, Integer)) => CoursePrerequisite(edge._1, edge._2)
        )(TC(prerequisites)(prerequisites.CourseId, prerequisites.PrerequisiteId))
        transitiveClosure.addObserver(o);

        // 001\ -> 002 -> 003 -> / -> 005
        //     \    -> 004  ->  /
        // check that the observer was correctly added

        prerequisites += CoursePrerequisite(001, 002)
        prerequisites += CoursePrerequisite(002, 003)
        prerequisites += CoursePrerequisite(003, 005)
        prerequisites += CoursePrerequisite(004, 005)


        o.events should be(List(
            AddEvent(CoursePrerequisite(4, 5)),
            AddEvent(CoursePrerequisite(1, 5)),
            AddEvent(CoursePrerequisite(2, 5)),
            AddEvent(CoursePrerequisite(3, 5)),
            AddEvent(CoursePrerequisite(1, 3)),
            AddEvent(CoursePrerequisite(2, 3)),
            AddEvent(CoursePrerequisite(1, 2))
        ))

        transitiveClosure.clearObserversForChildren(_ != prerequisites)

        // check that the the observer was correctly removed
        prerequisites.observers should have size (0)
    }


    @Test
    def testSelfMaintainedAggregationClosureRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Option[Int]]
        

        val aggregation = γ(students, Count[Student]())
        aggregation.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark
        students += mark
        // the database has started with two students
        o.events should be(List(
            UpdateEvent(Some(3), Some(4)),
            UpdateEvent(Some(2), Some(3))
        ))

        aggregation.clearObserversForChildren(_ != students)

        // check that the the observer was correctly removed
        students.observers should have size (0)
    }

    @Test
    def testNotSelfMaintainedAggregationClosureRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Option[Int]]
        

        val aggregation = γ(students, Min((_:Student).Id.intValue()))
        aggregation.addObserver(o);

        // check that the observer was correctly added
        students += new Student(1, "Mark")
        students += new Student(0, "Mark")
        // the database has started with two students
        o.events should be(List(
            UpdateEvent(Some(1), Some(0)),
            UpdateEvent(Some(12345),Some(1))
        ))

        aggregation.clearObserversForChildren(_ != students)

        // check that the the observer was correctly removed
        students.observers should have size (0)
    }


    @Test
    def testSemiJoinRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

        val studentsA = students.copy
        val studentsB = students.copy

        val semijoin = ((studentsA, students.Name) ⋉(students.Name, studentsB))
        semijoin.addObserver(o);

        // check that the observer was correctly added
        val mark001 = new Student(00001, "Mark")
        studentsA += mark001
        studentsB += mark001
        o.events should be(List(
            AddEvent(mark001)
        ))

        semijoin.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != studentsA && o != studentsB
            }
        )

        // check that the the observer was correctly removed
        studentsA.observers should have size (0)
        studentsB.observers should have size (0)
    }


    @Test
    def testAntiSemiJoinRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

        val studentsA = students.copy
        val studentsB = students.copy

        val antisemi = ((studentsA, students.Name) ⊳(students.Name, studentsB))
        antisemi.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        studentsA += mark
        studentsB += mark
        o.events should be(List(
            RemoveEvent(mark),
            AddEvent(mark)
        ))


        antisemi.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != studentsA && o != studentsB
            }
        )

        // check that the the observer was correctly removed
        studentsA.observers should have size (0)
        studentsB.observers should have size (0)

    }

    @Test
    def testAntiSemiJoinRemovalwithConversions() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[Student]
        

        val studentsA = new BagExtent[Student]
        val studentsB = new BagExtent[Student]

        val antisemi = ((studentsA, students.Name) ⊳(students.Name, studentsB))
        antisemi.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        studentsA.element_added ( mark)
        studentsB.element_added ( mark )
        o.events should be(List(
            RemoveEvent(mark),
            AddEvent(mark)
        ))


        antisemi.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => {
                o != studentsA
            }
        )

        // check that the the observer was correctly removed
        studentsA.observers should have size (0)
        studentsB.observers should have size (0)

    }

    @Test
    def testCombinedSelectProjectRemoval() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[String]
        

        val selection = σ((_: Student).Name == "Mark")(students)
        val projection = Π((_: Student).Name)(selection)
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
    def testLeaveRequiredObservers() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[String]
        

        val selection = σ((_: Student).Name == "Mark")(students)
        val projection = Π((_: Student).Name)(selection)
        projection.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark
        o.events should be(List(AddEvent("Mark")))

        val otherInterestedParty : Observer[Student] = new MockObserver[Student]()
        selection.addObserver(otherInterestedParty)

        projection.clearObserversForChildren(_ != students)

        // check that the the observer was correctly removed
        students.observers should have size (1)
        students.observers should contain (selection.asInstanceOf[Observer[Student]])

        selection.observers should have size (1)
        selection.observers should contain (otherInterestedParty)
    }

    @Test
    def testStopAtChildren() {
        val database = new StudentCoursesDatabase()
        import database._
        val o = new MockObserver[String]
        

        val selection = σ((_: Student).Name == "Mark")(students)
        val projection = Π((_: Student).Name)(selection)
        projection.addObserver(o);

        // check that the observer was correctly added
        val mark = new Student(00001, "Mark")
        students += mark
        o.events should be(List(AddEvent("Mark")))

        projection.clearObserversForChildren(_ != selection)

        // check that the the observer was correctly removed
        students.observers should have size (1)
        students.observers should contain (selection.asInstanceOf[Observer[Student]])

        selection.observers should have size (0)
    }
}