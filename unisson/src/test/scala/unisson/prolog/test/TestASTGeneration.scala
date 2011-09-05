package unisson.prolog.test

import org.junit.Test
import org.junit.Assert._
import unisson.ast._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:40
 *
 */

class TestASTGeneration {

    import unisson.CheckArchitectureFromProlog._

    @Test
    def testSimpleGraphV1()
    {

        val definitions = readSadFile(resourceAsStream("unisson/prolog/test/simplegraph/v1/selfref/v1.selfref.ensemble.sad.pl"))
        val A = definitions.collectFirst{ case e @ Ensemble("A",_,_,_) => e }
        assertTrue( A != None )
        assertEquals( ClassWithMembersQuery(ClassQuery("unisson.test.simplegraph.v1.selfref","A")), A.get.query )
    }

    @Test
    def testSimpleGraphV2Cycle()
    {

        val definitions = readSadFile(resourceAsStream("unisson/prolog/test/simplegraph/v2/cycle/v2.cycle.expected_correct.sad.pl"))

        val A = definitions.collectFirst{ case e @ Ensemble("A",_,_,_) => e }
        assertTrue( A != None )
        // TODO refactor model and query
        assertEquals( ClassWithMembersQuery(ClassQuery("opal.test.simplegraph.v2.cycle","A")), A.get.query )



        val B = definitions.collectFirst{ case e @ Ensemble("B",_,_,_) => e }
        assertTrue( B != None )
        // TODO refactor model and query
        assertEquals( ClassWithMembersQuery(ClassQuery("opal.test.simplegraph.v2.cycle","B")), B.get.query )



    }
}