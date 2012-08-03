package sae.test

import org.junit.{Assert, Test}
import sae.collections.QueryResult
import sae.syntax.sql._

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

        val database = new StudentCoursesDatabase()

        import database._

        val students = database.students.copy // make a local copy

        def Name(s: Student) = s.Name


        val names1: QueryResult[String] = SELECT {
            (_: Student).Name
        } FROM students

        val names2: QueryResult[String] = SELECT {
            Name(_: Student)
        } FROM students

        val names3: QueryResult[String] = FROM(students) SELECT {
            Name(_)
        }

        val names4: QueryResult[String] = FROM(students) SELECT {(_:Student).Name}

        Assert.assertEquals(2, names1.size)
        Assert.assertEquals(2, names2.size)
        Assert.assertEquals(2, names3.size)

    }

    @Test
    def testProjectStarSyntax() {

        val database = new StudentCoursesDatabase()

        import database._

        val students = database.students.copy // make a local copy

        val allStudents: QueryResult[Student] = SELECT(*) FROM (students)

        Assert.assertEquals(2, allStudents.size)

    }

}