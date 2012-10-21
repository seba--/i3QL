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
        db.addClassFile(new FileInputStream("D:\\workspace\\java-demo\\bin\\test\\Test.class"))

        basicBlocks.foreach(println)
    }

}