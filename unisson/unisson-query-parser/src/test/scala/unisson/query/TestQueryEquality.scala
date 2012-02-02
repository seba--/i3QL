package unisson.query

import parser.QueryParser
import unisson.query.ast._

import org.junit.Test
import org.junit.Assert._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 14:12
 *
 */
class TestQueryEquality
{


    @Test
    def testSyntacticEqual_class_with_members() {
        val parser = new QueryParser()

        assertTrue(
            ClassWithMembersQuery(
                ClassSelectionQuery("org.hibernate.cache", "Cache")
            ).isSyntacticEqual(
                parser.parse(
                    "class_with_members(class('org.hibernate.cache','Cache'))"
                ).get
            )
        )
    }

    @Test
    def testSyntacticEqual_OrderOfTwoOrQueries() {
        val parser = new QueryParser()

        val queryA = parser.parse(
            "class_with_members(class('org.hibernate.cache','Cache')) or class_with_members(class('cirrus.hibernate.impl','HibernateProxy'))"
        ).get

        val queryB = parser.parse(
            "class_with_members(class('cirrus.hibernate.impl','HibernateProxy')) or class_with_members(class('org.hibernate.cache','Cache'))"
        ).get

        assertTrue(
            queryA.isSyntacticEqual(queryB)
        )

        assertTrue(
            queryB.isSyntacticEqual(queryA)
        )
    }


    @Test
    def testSyntacticEqual_OrderOfOrQueries() {
        val parser = new QueryParser()

        val queryA = parser.parse(
            "package('a') or package('b') or package('c') or package('d')"
        ).get

        val queryB = parser.parse(
            "package('c') or package('a') or package('b') or package('d')"
        ).get

        assertTrue(
            queryA.isSyntacticEqual(queryB)
        )

        assertTrue(
            queryB.isSyntacticEqual(queryA)
        )
    }

    @Test
    def testSyntacticDifferenceOfOrQueries() {
        val parser = new QueryParser()

        val queryA = parser.parse(
            "package('a') or package('b') or package('c')"
        ).get

        val queryB = parser.parse(
            "package('b') or package('c')"
        ).get

        assertFalse(
            queryA.isSyntacticEqual(queryB)
        )

        assertFalse(
            queryB.isSyntacticEqual(queryA)
        )
    }

    @Test
    def testSyntacticDifferenceOfOrQueriesWithRepetitions() {
        val parser = new QueryParser()

        val queryA = parser.parse(
            "package('a') or package('b') or package('a')"
        ).get

        val queryB = parser.parse(
            "package('a') or package('b') or package('b')"
        ).get

        assertFalse(
            queryA.isSyntacticEqual(queryB)
        )

        assertFalse(
            queryB.isSyntacticEqual(queryA)
        )
    }

}