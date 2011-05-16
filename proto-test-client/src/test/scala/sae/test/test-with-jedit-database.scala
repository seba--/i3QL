package sae.test
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert._

import sae.collections._
import sae.bytecode.model._
import de.tud.cs.st.bat._
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
        val query : QueryResult[ObjectType] = db.classfiles;
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

    @Test
    def count_method_calls : Unit = {
        val db = new JEditDatabase()
        val query : QueryResult[MethodCall] = db.method_calls;
        db.readBytecode
        assertEquals(44776, query.size)
    }
    
    @Test
    def count_internal_method_calls : Unit = {
        val db = new JEditDatabase()
        // the cross product would generate (7999 * 44776) ~ 350 million entries    
        // naive query // val query : QueryResult[MethodCall] = Π( (_:(MethodCall,Method))._1 )(db.method_calls ⋈( (_:(MethodCall,Method)) match {case (c:MethodCall, m:Method) => c.target == m} , db.classfile_methods));
        
        val query = ((db.method_calls, (_:MethodCall).target) ⋈ ( (m:Method) => m , db.classfile_methods)) { (c:MethodCall,m:Method) => c }

        db.readBytecode
        assertEquals(20358, query.size)
    }
    
    @Test
    def count_distinct_internal_method_calls : Unit = {
        val db = new JEditDatabase()
        // the cross product would generate (7999 * 44776) ~ 350 million entries    
        // naive query // val query : QueryResult[MethodCall] = Π( (_:(MethodCall,Method))._1 )(db.method_calls ⋈( (_:(MethodCall,Method)) match {case (c:MethodCall, m:Method) => c.target == m} , db.classfile_methods));
        
        val query = δ( Π( (c:MethodCall) => (c.source, c.target)) ( ((db.method_calls, (_:MethodCall).target) ⋈ ( (m:Method) => m , db.classfile_methods)) { (c:MethodCall,m:Method) => c } ))

        db.readBytecode
        assertEquals(14847, query.size)
    }
}