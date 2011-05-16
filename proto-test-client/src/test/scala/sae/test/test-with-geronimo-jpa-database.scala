package sae.test
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert._

import sae.collections._
import sae.bytecode.model._
import de.tud.cs.st.bat._
import sae.syntax.RelationalAlgebraSyntax._


class GeronimoJPADatabase extends sae.bytecode.BytecodeDatabase
{

    def readBytecode : Unit = 
    {
		addArchiveAsResource("geronimo-jpa_1.0_spec-1.1.2.jar")
    }
    
}
// import org.scalatest.junit.JUnitRunner// @RunWith(classOf[JUnitRunner]) 
class GeronimoJPASuite {

    @Test
    def count_classfiles : Unit = {
        val db = new GeronimoJPADatabase()
        // TODO QueryResult
        val query : QueryResult[ObjectType] = db.classfiles;
        db.readBytecode
        assertEquals(91, query.size);
    }

    @Test
    def count_classfile_methods : Unit = {
        val db = new GeronimoJPADatabase()
        val query : QueryResult[Method] = db.classfile_methods;
        db.readBytecode
        //query.foreach(println)
        assertEquals(265, query.size)
    }
}