package sae.test
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert._

import sae.collections._
import sae.bytecode.model._
import sae.syntax.RelationalAlgebraSyntax._


class JEditDatabase extends sae.bytecode.BytecodeDatabase
{

    def readBytecode : Unit = 
    {
		addArchiveAsResource("jedit-4.3.3-win.jar")
    }
    
}
// import org.scalatest.junit.JUnitRunner// @RunWith(classOf[JUnitRunner]) 
class JEditSuite {

    @Test
    def count_classfiles : Unit = {
        val db = new JEditDatabase()
        val query : QueryResult[ClassFile] = db.classfiles;
        db.readBytecode
        assertEquals(1132, query.size);
    }

    @Test
    def count_classfile_methods : Unit = {
        val db = new JEditDatabase()
        val query : QueryResult[Method] = db.classfile_methods;
        db.readBytecode
        assertEquals(7999, query.size)
    }
}