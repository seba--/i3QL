package unisson.prolog.parser

import org.junit.Test
import org.junit.Assert._
import unisson.ast._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 14:12
 *
 */

class TestPrologParser
{


    @Test
    def testParseAtom()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals("a", parser.parseAll(atom, "a").get)

        assertEquals("a", parser.parseAll(atom, "'a'").get)

        assertEquals("Atom", parser.parseAll(atom, "'Atom'").get)

        assertEquals("my_atom", parser.parseAll(atom, "my_atom").get)

        assertEquals("my.atom", parser.parseAll(atom, "'my.atom'").get)
    }


    @Test
    def testParseAtomList()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals(Nil, parser.parseAll(atomList, "[]").get)

        assertEquals(List("a"), parser.parseAll(atomList, "[a]").get)

        assertEquals(List("a"), parser.parseAll(atomList, "['a']").get)

        assertEquals(List("a", "n"), parser.parseAll(atomList, "['a', n]").get)

        assertEquals(List("a", "n", "x", "a"), parser.parseAll(atomList, "['a', n, 'x', a]").get)
    }

    @Test
    def testParseOrQuery()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals(
            OrQuery(PackageQuery("java.lang"), PackageQuery("java.reflect")),
            parser.parseAll(query, "package('java.lang') or package('java.reflect')").get
        )
    }


    @Test
    def testParseWithoutQuery()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals(
            OrQuery(
                WithoutQuery(
                    PackageQuery("org.hibernate.id"),
                    ClassWithMembersQuery(ClassQuery("org.hibernate.id", "IdentifierGenerationException"))
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
    def testTransitiveSupertypeQuery()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals(
            OrQuery(
                WithoutQuery(
                    PackageQuery("org.hibernate.id"),
                    ClassWithMembersQuery(ClassQuery("org.hibernate.id", "IdentifierGenerationException"))
                ),
                OrQuery(
                    PackageQuery("org.hibernate.id.uuid"),
                    OrQuery(PackageQuery("org.hibernate.id.enhanced"), PackageQuery("org.hibernate.id.insert"))
                )
            )
            ,
            parser.parseAll(
                query,
                "class_with_members(transitive(supertype(class('org.hibernate.cache','Cache'))))"
            ).get
        )
    }
}