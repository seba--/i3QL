package sae.test

import org.junit.{Ignore, Assert, Test}
import sae.syntax.sql._
import sae.LazyView
import scala.Some
import runtime.Boxed

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 18:23
 *
 */
class SQLSyntaxTest
{

    @Test
    def testProjectSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        def Name(s: Student) = s.Name


        val names1: LazyView[String] = SELECT {
            (_: Student).Name
        } FROM students

        val names2: LazyView[String] = SELECT {
            Name (_: Student)
        } FROM students

        /*
        val names3: LazyView[String] = FROM (students) SELECT {
            Name (_)
        }

        def SName: Student => String = s => s.Name

        // the scala compiler can not infer the type of the anonymous function, because the function needs a type before it is passed as parameter
        val names4: LazyView[String] = FROM (students) SELECT ((_: Student).Name)

        // but we can do this
        val names5: LazyView[String] = FROM (students) SELECT (SName)
        */
        Assert.assertEquals (2, names1.size)
        Assert.assertEquals (2, names2.size)
        /*
        Assert.assertEquals (2, names3.size)
        Assert.assertEquals (2, names4.size)
        Assert.assertEquals (2, names5.size)
          */
    }

    @Test
    def testProjectFunctionTuplesFieldsSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        def Name: Student => String = s => s.Name

        def Id: Student => Integer = s => s.Id

        // but we can do this
        val select: LazyView[(String, Integer)] = SELECT ((Name, Id)) FROM (students)

        Assert.assertEquals (2, select.size)

    }

    @Test
    def testProjectStarSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val allStudents: LazyView[Student] = SELECT (*) FROM (students)

        Assert.assertEquals (2, allStudents.size)

    }


    @Test
    def testProjectDistinctStarSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        // add sally twice
        students += sally

        val allStudents: LazyView[Student] = SELECT DISTINCT (*) FROM (students)

        Assert.assertEquals (2, allStudents.size)

    }


    @Test
    def testDistinctProjectSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        // add sally twice
        students += sally

        def Name: Student => String = s => s.Name

        val names: LazyView[String] = SELECT DISTINCT (Name) FROM (students)

        Assert.assertEquals (2, names.size)
    }

    @Test
    def testFilterSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val selection: LazyView[Student] = SELECT (*) FROM (students) WHERE (_.Name == "sally")

        Assert.assertEquals (1, selection.size)

        Assert.assertEquals (Some (sally), selection.singletonValue)

    }

    @Test
    def testDistinctFilterSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy
        students += sally

        val selection: LazyView[Student] = SELECT DISTINCT (*) FROM (students) WHERE (_.Name == "sally")

        Assert.assertEquals (1, selection.size)

        Assert.assertEquals (Some (sally), selection.singletonValue)

    }

    @Test
    def testMultipleFilterConjunctionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val selection: LazyView[Student] = SELECT (*) FROM (students) WHERE (_.Name == "sally") AND (_.Id == 12346)

        val selectionNative: LazyView[Student] = SELECT (*) FROM (students) WHERE ((s: Student) => s.Name == "sally" && s
            .Id == 12346)

        Assert.assertEquals (1, selection.size)

        Assert.assertEquals (Some (sally), selection.singletonValue)

        Assert.assertEquals (selection.asList, selectionNative.asList)

    }


    @Test
    def testMultipleFilterDisjunctionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val selection: LazyView[Student] = SELECT (*) FROM (students) WHERE (_.Name == "sally") OR (_.Id == 12345)

        Assert.assertEquals (2, selection.size)

        Assert.assertEquals (
            List (john, sally),
            selection.asList.sortBy (_.Id)
        )

    }

    @Test
    def testMultipleFilterCovarianceSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        def Name: Person => String = person => person.Name

        val selection: LazyView[String] = SELECT (Name) FROM (students) WHERE (_.Name == "sally") OR (_.Id == 12345)

        Assert.assertEquals (2, selection.size)

        Assert.assertEquals (
            List ("john", "sally"),
            selection.asList.sorted
        )

    }

    @Test
    def testMultipleFilterInlineSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val sally2 = Student (636363, "sally")
        students += sally2

        val selection: LazyView[Student] = SELECT (*) FROM (students) WHERE (_.Name == "sally") AND (((_: Student).Id == 12346) OR (_.Id == 636363))

        Assert.assertEquals (2, selection.size)

        Assert.assertEquals (
            List (sally, sally2),
            selection.asList.sortBy (_.Id)
        )

    }


    @Test
    def testMultipleFilterDisjunctionWithProjectionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        def Name: Student => String = x => x.Name

        val selection: LazyView[String] = SELECT (Name) FROM (students) WHERE (_.Name == "sally") OR (_.Id == 12345)

        Assert.assertEquals (2, selection.size)

        Assert.assertEquals (
            List ("john", "sally"),
            selection.asList.sorted
        )

    }


    @Test
    def testCrossProductStartAtFromWithProjectionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val courses = database.courses.copy // make a local copy

        def Name: Student => String = x => x.Name

        def CourseName: Course => String = x => x.Name

        val selection1: LazyView[(String, String)] = SELECT ((s: Student, c: Course) => (s
            .Name, c.Name)) FROM (students, courses)

        // this causes ambiguites with the select distinct syntax
        //val selection2: LazyView[(String, String)] = FROM (students, courses) SELECT ((Name, CourseName))

        val selection3: LazyView[(String, String)] = SELECT (Name, CourseName) FROM (students, courses)
        Assert.assertEquals (
            List (
                ("john", "EiSE"),
                ("john", "SE-D&C"),
                ("sally", "EiSE"),
                ("sally", "SE-D&C")
            ),
            selection1.asList.sorted
        )

        //Assert.assertEquals (selection1.asList.sorted, selection2.asList.sorted)

        Assert.assertEquals (selection1.asList.sorted, selection3.asList.sorted)
    }

    @Test
    def testDistinctCrossProductStartAtFromWithProjectionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        students += sally

        val courses = database.courses.copy // make a local copy

        def Name: Student => String = x => x.Name

        def CourseName: Course => String = x => x.Name

        val selection1: LazyView[(String, String)] =
            SELECT DISTINCT ((s: Student, c: Course) => (s.Name, c.Name)) FROM (students, courses)


        // this causes ambiguites with the select syntax
        //val selection2: LazyView[(String, String)] = FROM (students, courses) SELECT DISTINCT ((Name, CourseName))

        val selection3: LazyView[(String, String)] =
            SELECT DISTINCT (Name, CourseName) FROM (students, courses)

        Assert.assertEquals (
            List (
                ("john", "EiSE"),
                ("john", "SE-D&C"),
                ("sally", "EiSE"),
                ("sally", "SE-D&C")
            ),
            selection1.asList.sorted
        )

        //Assert.assertEquals (selection1.asList.sorted, selection2.asList.sorted)

        Assert.assertEquals (selection1.asList.sorted, selection3.asList.sorted)

    }

    @Test
    def testCrossProductStartAtFromNoProjectionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val courses = database.courses.copy // make a local copy

        val selection1: LazyView[(Student, Course)] = SELECT (*) FROM (students, courses)

        Assert.assertEquals (
            List (
                (john, eise),
                (john, sed),
                (sally, eise),
                (sally, sed)
            ),
            selection1.asList.sortBy (x => (x._1.Name, x._2.Name))
        )

    }

    @Test
    def testDistinctCrossProductStartAtFromNoProjectionSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        students += sally

        val courses = database.courses.copy // make a local copy

        val selection1: LazyView[(Student, Course)] = SELECT DISTINCT (*) FROM (students, courses)

        Assert.assertEquals (
            List (
                (john, eise),
                (john, sed),
                (sally, eise),
                (sally, sed)
            ),
            selection1.asList.sortBy (x => (x._1.Name, x._2.Name))
        )

    }


    @Test
    def testDisjunctionsFlatNoSubQuery() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val courses = database.courses.copy // make a local copy

        val selection: LazyView[(Student, Course)] =
            SELECT (*) FROM (students, courses) WHERE (_.Name == "john") OR (_.Name == "sally") OR ((_: Course).Name == "EiSE") OR (_.Name == "SE-D&C")

        Assert.assertEquals (
            List (
                (john, eise),
                (john, sed),
                (sally, eise),
                (sally, sed)
            ),
            selection.asList.sortBy (x => (x._1.Name, x._2.Name))
        )
    }

    @Test
    def testDisjunctionsNestedNoSubQuery() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val courses = database.courses.copy // make a local copy

        val selection: LazyView[(Student, Course)] =
            SELECT (*) FROM (students, courses) WHERE (_.Name == "john") OR (_.Name == "sally") OR (((_: Course).Name == "EiSE") OR (_.Name == "SE-D&C"))

        Assert.assertEquals (
            List (
                (john, eise),
                (john, sed),
                (sally, eise),
                (sally, sed)
            ),
            selection.asList.sortBy (x => (x._1.Name, x._2.Name))
        )
    }


    @Test
    def testJoinSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val query: LazyView[(Student, Enrollment)] =
            SELECT (*) FROM (students, enrollments) WHERE ((_: Student).Id) === ((_: Enrollment).StudentId)

        val join = ((_: Student).Id) === ((_: Enrollment).StudentId)

        val queryWithPreparedJoin: LazyView[(Student, Enrollment)] =
            SELECT (*) FROM (students, enrollments) WHERE join


        Assert.assertEquals (
            List (
                (john, Enrollment (john.Id, eise.Id)),
                (sally, Enrollment (sally.Id, eise.Id)),
                (sally, Enrollment (sally.Id, sed.Id))
            ),
            query.asList.sortBy (x => (x._1.Name, x._2.CourseId))
        )

        Assert.assertEquals (
            List (
                (john, Enrollment (john.Id, eise.Id)),
                (sally, Enrollment (sally.Id, eise.Id)),
                (sally, Enrollment (sally.Id, sed.Id))
            ),
            queryWithPreparedJoin.asList.sortBy (x => (x._1.Name, x._2.CourseId))
        )
    }

    @Test
    def testJoinSyntaxWithSelection() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val query: LazyView[(Student, Enrollment)] =
            SELECT (*) FROM (students, enrollments) WHERE (_.Name == "sally") AND ((_: Student).Id) === ((_: Enrollment).StudentId)

        Assert.assertEquals (
            List (
                (sally, Enrollment (sally.Id, eise.Id)),
                (sally, Enrollment (sally.Id, sed.Id))
            ),
            query.asList.sortBy (x => (x._1.Name, x._2.CourseId))
        )
    }

    @Test
    @Ignore
    def testConjunctiveExistsWithoutJoin() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val query: LazyView[Student] =
            SELECT (*) FROM (students) WHERE (_.Name == "sally") AND EXISTS (SELECT (*) FROM (enrollments) WHERE (_.StudentId == 12346))

        Assert.assertEquals (
            List (
                (sally)
            ),
            query.asList
        )
    }

    @Test
    def testConjunctiveExistsWithJoin ()
    {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val subQuery: SQL_QUERY_UNBOUND_1[Enrollment, Student, Enrollment] = SELECT (*) FROM (enrollments) WHERE ((_: Enrollment).StudentId) === ((_: Student).Id)

        val query1: LazyView[Student] =
            SELECT (*) FROM (students) WHERE (_.Name == "sally") AND EXISTS (subQuery)

        // scala compiles this, intellij not
        val query2: LazyView[Student] =
            SELECT (*) FROM (students) WHERE (_.Name == "sally") AND
                EXISTS (SELECT (*) FROM (enrollments) WHERE ((_: Enrollment).StudentId) === ((_: Student).Id))

        Assert.assertEquals (
            List (
                (sally)
            ),
            query1.asList
        )

        Assert.assertEquals (
            List (
                (sally)
            ),
            query2.asList
        )

    }

    @Test
    def testConjunctiveNotExistsWithJoin() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val query: LazyView[Student] =
            SELECT (*) FROM (students) WHERE (_.Name == "sally") AND NOT (
                EXISTS (SELECT (*) FROM (enrollments) WHERE ((_: Enrollment).StudentId) === ((_: Student).Id))
            )

        Assert.assertEquals (
            List (),
            query.asList
        )
    }


    @Test
    @Ignore
    def testJoinNegationSyntaxWithSelection ()
    {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val query: LazyView[(Student, Enrollment)] =
            SELECT (*) FROM (students, enrollments) WHERE (_.Name == "john") AND NOT (((_: Student).Id) === ((_: Enrollment).StudentId)) // NOT(((_: Student).Id) === ((_: Enrollment).StudentId))
        Assert.assertEquals (
            List (
                (john, Enrollment (john.Id, sed.Id))
            ),
            query.asList.sortBy (x => (x._1.Name, x._2.CourseId))
        )
    }


    @Test
    def testAggregationCount() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val enrollments = database.enrollments.copy // make a local copy

        val query: LazyView[Some[Int]] =
            SELECT COUNT (*) FROM (students, enrollments) WHERE (_.Name == "sally") AND ((_: Student).Id) === ((_: Enrollment).StudentId)

        Assert.assertEquals (
            List (
                (2)
            ),
            query.asList
        )
    }

}