package sae.test

import org.junit.{Assert, Test}
import sae.syntax.SQLSyntax
import sae.collections.QueryResult

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 18:23
 *
 */
class SQLSyntaxTest
{

    import SQLSyntax._



    @Test
    def testProjectSyntax() {

        val database = new StudentCoursesDatabase()

        import database._

        val students = database.students.copy // make a local copy

        val names: QueryResult[String] = SELECT ((_: Student).Name) FROM students

        Assert.assertEquals(2, names.size)

    }

    @Test
    def testProjectStarSyntax() {

        val database = new StudentCoursesDatabase()

        import database._

        val students = database.students.copy // make a local copy

        val allStudents: QueryResult[Student] = SELECT {*[Student]} FROM students

        Assert.assertEquals(2, allStudents.size)

    }

}