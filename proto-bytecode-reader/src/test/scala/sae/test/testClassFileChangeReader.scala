package sae.test

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import sae.reader._
import java.io.File

class TestClassFileChangeReader extends org.scalatest.junit.JUnitSuite {
    val location = "./src/main/recources/smallTestSet"
    
    @Test
    def readAndGroupSomeTestData() {

        //TODO what is the maven way for doing something like this
        val reader = new ClassFileChangeEventReader(new File(location))
    	var res : List[List[Event]]=  reader.getAllFilesGroupedByEventTime(new File(location))
    	var lastEventTime : Long = 0
    	var sumEvents = 0
    	var sumEventFiles = 0
    	res.foreach(x => {
    	    //check that the event time increase with every event
    		assertTrue(x.head.eventTime > lastEventTime)
    	    lastEventTime = x.head.eventTime
    	    sumEvents += 1
    	    x.foreach( y => {
    	        //check that the eventTime is the same for all eventfiles in one event
    	      assertTrue(y.eventTime == lastEventTime)
    	      sumEventFiles += 1
    	    })
    	})
    	assertTrue(sumEvents == 6)
    	assertTrue(sumEventFiles == 85)
    	
    }
    @Test
    def applyFonAllEvents() {
        var i = 0
        val reader = new ClassFileChangeEventReader(new File(location))
        reader.foreach(println _ )
    }

}