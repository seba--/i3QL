package sae.java.test

import code.{WorkerImpl, AbstractWorker, IWorker, HelloWorld}
import org.junit.Test
import org.junit.Assert._
import sae.java.{Package, BytecodeDatabase}
import sae.java.types.Class

/**
 * 
 * Author: Ralf Mitschke
 * Created: 29.08.11 10:48
 *
 */

class TestJavaCode {



    @Test
    def testLoadMainClass() {
        val db = new BytecodeDatabase
        db.addClasses( classOf[HelloWorld] )(db.classAsStream)
        assertTrue( db.packages.contains(Package("sae.java.test.code")) )
        assertTrue( db.packages.contains(Package("java.lang")) )
        assertEquals(2, db.packages.size)
        val members = db.packages.find( _.Name == "sae.java.test.code").get.Members

        assertTrue( members.contains(Class(Package("sae.java.test.code"), "HelloWorld")) )

        val helloWorld = members.find( _.Name == "HelloWorld" ).get
        assertEquals( Class(Package("java.lang"), "Object"),  helloWorld.SuperType.get )


        assertEquals(3, helloWorld.Methods.size)


    }


    @Test
    def testHierarchy() {
        val db = new BytecodeDatabase
        db.addClasses( classOf[IWorker], classOf[AbstractWorker], classOf[WorkerImpl] )(db.classAsStream)
        assertTrue( db.packages.contains(Package("sae.java.test.code")) )
        val members = db.packages.find( _.Name == "sae.java.test.code").get.Members

        val iWorker = members.find( _.Name == "IWorker" ).get
        val abstractWorker = members.find( _.Name == "AbstractWorker" ).get
        val workerImpl = members.find( _.Name == "WorkerImpl" ).get


        assertEquals( abstractWorker, workerImpl.SuperType.get)
        assertEquals( 1, abstractWorker.Interfaces.size )
        assertTrue( abstractWorker.Interfaces.contains(iWorker) )
        assertEquals( 0, workerImpl.Interfaces.size ) // does not implement the interface directly

    }

}