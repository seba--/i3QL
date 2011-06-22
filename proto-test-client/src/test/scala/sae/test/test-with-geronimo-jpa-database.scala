package sae
package test
import org.junit.Test
import org.junit.runner.RunWith
import de.tud.cs.st.bat.ObjectType
import bytecode.model.Method
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import sae.collections._
import sae.syntax.RelationalAlgebraSyntax._


class GeronimoJPADatabase extends sae.bytecode.BytecodeDatabase
{

    def readBytecode : Unit = 
    {
		addArchiveAsResource("geronimo-jpa_1.0_spec-1.1.2.jar")
    }
    
}


class GeronimoJPASuite extends org.scalatest.junit.JUnitSuite {

    @Test
    def count_classfiles : Unit = {
        val db = new GeronimoJPADatabase()
        val query : QueryResult[ObjectType] = db.classfiles;
        db.readBytecode
        assertEquals(91, query.size);
    }

    @Test
    def count_classfile_methods : Unit = {
        val db = new GeronimoJPADatabase()
        val query : QueryResult[Method] = db.classfile_methods;
        db.readBytecode
        assertEquals(265, query.size)
    }
}