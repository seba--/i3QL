package test

import sae.bytecode.bat.BATDatabaseFactory
import java.io.FileInputStream
import org.junit.Test
import sae.QueryResult
import sae.bytecode.structure.CodeInfo

/**
 * 
 * Author: Ralf Mitschke
 * Date: 18.10.12
 * Time: 18:23
 *
 */
class TestCfg {

    @Test
    def testCfg() {
        val db = BATDatabaseFactory.create()
        val basicBlocks = db.basicBlocksNew
        val clazz = this.getClass.getClassLoader.getResource("sae/test/code/innerclass/MyRootClass.class")

        val code : QueryResult[CodeInfo] = db.code

        db.addClassFile(clazz.openStream())

        //basicBlocks.foreach(println)
        code.foreach(println)
    }

}