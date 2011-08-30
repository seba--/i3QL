package unisson.prolog.parser

import org.junit.Test
import org.junit.Assert._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 14:12
 *
 */

class TestPrologParser {

    @Test
    def testParseAtom()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals("a", parser.parse(atom, "a").get)

        assertEquals("a", parser.parse(atom, "'a'").get)

        assertEquals("Atom", parser.parse(atom, "'Atom'").get)

        assertEquals("my_atom", parser.parse(atom, "my_atom").get)

        assertEquals("my.atom", parser.parse(atom, "'my.atom'").get)
    }


    @Test
    def testParseAtomList()
    {
        val parser = new UnissonPrologParser()

        import parser._

        assertEquals(Nil, parser.parse(atomList, "[]").get)

        assertEquals(List("a"), parser.parse(atomList, "[a]").get)

        assertEquals(List("a"), parser.parse(atomList, "['a']").get)

        assertEquals(List("a", "n"), parser.parse(atomList, "['a', n]").get)

        assertEquals(List("a", "n", "x", "a"), parser.parse(atomList, "['a', n, 'x', a]").get)
    }
}