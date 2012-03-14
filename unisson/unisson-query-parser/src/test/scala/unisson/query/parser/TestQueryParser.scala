package unisson.query.parser


import unisson.query.ast._

import org.junit.Test
import org.junit.Assert._
import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 14:12
 *
 */
class TestQueryParser
{


    @Test
    def testParseAtom() {
        val parser = new QueryParser()

        import parser._

        assertEquals("a", parser.parseAll(atom, "a").get)

        assertEquals("a", parser.parseAll(atom, "'a'").get)

        assertEquals("Atom", parser.parseAll(atom, "'Atom'").get)

        assertEquals("my_atom", parser.parseAll(atom, "my_atom").get)

        assertEquals("my.atom", parser.parseAll(atom, "'my.atom'").get)
    }


    @Test
    def testParseOrQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            OrQuery(PackageQuery("java.lang"), PackageQuery("java.reflect")),
            parser.parseAll(query, "package('java.lang') or package('java.reflect')").get
        )
    }


    @Test
    def testParseWithoutQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            OrQuery(
                WithoutQuery(
                    PackageQuery("org.hibernate.id"),
                    ClassWithMembersQuery(ClassSelectionQuery("org.hibernate.id", "IdentifierGenerationException"))
                ),
                OrQuery(
                    PackageQuery("org.hibernate.id.uuid"),
                    OrQuery(PackageQuery("org.hibernate.id.enhanced"), PackageQuery("org.hibernate.id.insert"))
                )
            )
            ,
            parser.parseAll(
                query,
                "(package('org.hibernate.id') without class_with_members('org.hibernate.id','IdentifierGenerationException')) or package('org.hibernate.id.uuid') or package('org.hibernate.id.enhanced') or package('org.hibernate.id.insert')"
            ).get
        )
    }

    @Test
    def testClassWithMembersSubqueryQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            ClassWithMembersQuery(
                ClassSelectionQuery("org.hibernate.cache", "Cache")
            )
            ,
            parser.parseAll(
                query,
                "class_with_members(class('org.hibernate.cache','Cache'))"
            ).get
        )
    }

    @Test
    def testTransitiveQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            TransitiveQuery(
                ClassSelectionQuery("org.hibernate.cache", "Cache")
            )
            ,
            parser.parseAll(
                query,
                "transitive(class('org.hibernate.cache','Cache'))"
            ).get
        )
    }

    @Test
    def testTransitiveSupertypeQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            ClassWithMembersQuery(
                TransitiveQuery(
                    SuperTypeQuery(
                        ClassSelectionQuery("org.hibernate.cache", "Cache")
                    )
                )
            )
            ,
            parser.parseAll(
                query,
                "class_with_members(transitive(supertype(class('org.hibernate.cache','Cache'))))"
            ).get
        )
    }


    @Test
    def testFieldQueryWithClassQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            FieldQuery(
                ClassSelectionQuery("org.hibernate.cache", "BlockingCache"),
                "lockedKeys",
                ClassSelectionQuery("java.util", "HashMap")
            )
            ,
            parser.parseAll(
                query,
                "field(class('org.hibernate.cache','BlockingCache'), lockedKeys, class('java.util','HashMap'))"
            ).get
        )
    }


    @Test
    def testFieldQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            FieldQuery(
                ClassSelectionQuery("org.hibernate.cache", "BlockingCache"),
                "lockedKeys",
                ClassSelectionQuery("java.util", "HashMap")
            )
            ,
            parser.parseAll(
                query,
                "field('org.hibernate.cache','BlockingCache', lockedKeys, class('java.util','HashMap'))"
            ).get
        )
    }

    @Test
    def testMethodQueryWithClassQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            MethodQuery(
                ClassSelectionQuery("org.hibernate.cache", "Cache"),
                "put",
                TypeQuery("void"),
                List(
                    ClassSelectionQuery("java.lang", "Object"),
                    ClassSelectionQuery("java.lang", "Object")
                ) :_*
            )
            ,
            parser.parseAll(
                query,
                "method(class('org.hibernate.cache','Cache'), put, void, [class('java.lang','Object'),class('java.lang','Object') ])"
            ).get
        )
    }

    @Test
    def testMethodQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            MethodQuery(
                ClassSelectionQuery("org.hibernate.cache", "Cache"),
                "put",
                TypeQuery("void"),
                List(
                    ClassSelectionQuery("java.lang", "Object"),
                    ClassSelectionQuery("java.lang", "Object")
                ) :_*
            )
            ,
            parser.parseAll(
                query,
                "method('org.hibernate.cache','Cache', put, void, [class('java.lang','Object'),class('java.lang','Object') ])"
            ).get
        )
    }

    @Test
    def testMethodWithTypeQuery() {
        val parser = new QueryParser()

        import parser._

        assertEquals(
            MethodQuery(
                ClassSelectionQuery("org.hibernate.cache", "Cache"),
                "put",
                TypeQuery("void"),
                List(
                    TypeQuery("java.lang.Object"),
                    TypeQuery("java.lang.Object")
                ) :_*
            )
            ,
            parser.parseAll(
                query,
                "method('org.hibernate.cache','Cache', put, void, ['java.lang.Object','java.lang.Object' ])"
            ).get
        )
    }
}