package test

import sae.bytecode.bat.BATDatabaseFactory
import java.io.FileInputStream
import org.junit.Test

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
        db.addClassFile(clazz.openStream())

        basicBlocks.foreach(println)
    }

}