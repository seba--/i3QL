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

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        def Name(s: Student) = s.Name


        val names1: QueryResult[String] = SELECT {
            (_: Student).Name
        } FROM students

        val names2: QueryResult[String] = SELECT {
            Name (_: Student)
        } FROM students

        val names3: QueryResult[String] = FROM (students) SELECT {
            Name (_)
        }

        def SName: Student => String = s => s.Name

        // the scala compiler can not infer the type of the anonymous function, because the function needs a type before it is passed as parameter
        val names4: QueryResult[String] = FROM (students) SELECT ((_: Student).Name)

        // but we can do this
        val names5: QueryResult[String] = FROM (students) SELECT (SName)

        Assert.assertEquals (2, names1.size)
        Assert.assertEquals (2, names2.size)
        Assert.assertEquals (2, names3.size)

    }

    @Test
    def testProjectFunctionTuplesFieldsSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        def Name: Student => String = s => s.Name

        def Id: Student => Integer = s => s.Id

        // but we can do this
        val select: QueryResult[(String, Integer)] = FROM (students) SELECT {
            (Name, Id)
        }

        Assert.assertEquals (2, select.size)

    }

    @Test
    def testProjectStarSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        val allStudents: QueryResult[Student] = SELECT (*) FROM (students)

        Assert.assertEquals (2, allStudents.size)

    }


    @Test
    def testProjectDistinctStarSyntax() {

        val database = new StudentCoursesDatabase ()

        import database._

        val students = database.students.copy // make a local copy

        // add sally twice
        students += sally

        val allStudents: QueryResult[Student] = SELECT DISTINCT (*) FROM (students)

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

        val names: QueryResult[String] = FROM (students) SELECT DISTINCT (Name)

        Assert.assertEquals (2, names.size)
    }

}