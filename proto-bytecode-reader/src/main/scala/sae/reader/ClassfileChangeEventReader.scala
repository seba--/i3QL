package sae.reader
import java.io.File

import scala.collection.immutable.SortedMap

// resolvedClassName := package/subpackage/ClassName
case class Event(val eventType : String, val eventTime : Long, val resolvedClassName : String, val eventFile : File) {
    
}


/**
 * 
 * reading all classfiles (from the form TIMESTAMP_EVENTTYPE_NAME.class [output of ClassFileChangeTracker]) in a dir and all subdirs and
 * grouping this classfiles in Events
 * @param location : location of the "main" dir
 * IMPORTANT: location must be the folder of the default packages	
 * @author Malte V
 */
class ClassFileChangeEventReader(val location : File) {

    

    def foreach(f : EventSet => _) : Unit = {
        getAllFilesGroupedByEventTime(location).foreach(x => f(eventFilesToEvent(x)))
    }
    /**
     * converts a list of Events into one Event
     */
    private def eventFilesToEvent(eventSet : List[Event]) : EventSet = {
        new EventSet(eventSet)
    }

    /**
     * method that returns all files in a dir and all sub dirs wrapped in Events,
     * grouped by the event time as a List of Lists (one list for every real event [real events can contain
     * more then one Event])
     * ONLY PUBLIY FOR TESTING
     */
    def getAllFilesGroupedByEventTime(currentLocation : File) = {
        var list = List[List[Event]]()
        var sortedFiles = getAllFilesSortedByEventTime(currentLocation)
        var lastFile : Option[Event] = None
        var subList = List[Event]()
        for (eventFile <- sortedFiles) {
            lastFile match {
                case None => {
                    subList = List[Event]()
                    subList = eventFile :: subList
                    lastFile = Some(eventFile)
                }
                case Some(x) => {
                    if (x.eventTime == eventFile.eventTime) {
                        subList = eventFile :: subList
                    } else {
                        list = subList :: list
                        subList = List[Event]()
                        subList = eventFile :: subList
                        lastFile = Some(eventFile)
                    }
                }

            }

        }
        list

    }

    /**
     * return a list with all Events in a given dir and all sub dirs
     * sorted by the event time
     */
    private def getAllFilesSortedByEventTime(currentLocation : File) : Array[Event] = {
        var allEvents = readAllFiles(currentLocation)
        val sorted = scala.util.Sorting.stableSort(allEvents, (f : Event) => f.eventTime)
        sorted
        sorted.reverse // (ascending order)
    }

    /**
     * reads all files at an given location and all sub dirs
     * @param currentLocation : the start dir
     * @return a List with all files in the location and all sub dirs wrapped in a Event
     */
    private def readAllFiles(currentLocation : File) : List[Event] = {
        var list = List[Event]()
        if (currentLocation.isDirectory) {
            for (file <- currentLocation.listFiles) {
                list = readAllFiles(file) ::: list
            }
        } else {
            if (checkFile(currentLocation))
                list = fileToEvent(currentLocation) :: list
        }
        list
    }

    /**
     * checks if a given file could be wrote from ClassFileChangeTracker
     */
    private def checkFile(file : File) : Boolean = {
        if (file.isDirectory)
            false
        if (!file.getName().endsWith("class"))
            false
        //TODO discuss if the check needs to be extended
        //should normally only used on dir that were created by ClassfileChangeTracker
        true
    }
    /**
     * wraps a file into an Event
     * should only be called for class files that are created by the ClassFilechangeTracker
     * @throws ConvertFileToEventException : if the file is not convertible to an Event
     */
    private def fileToEvent(file : File) : Event = {
        val SEPARATOR = "_"
        //"calc" resolved full class name (package/subpackage/.../className 
        val loc = location.getCanonicalPath
        val dest = file.getParentFile.getCanonicalPath()
        var packages = dest.drop(loc.length).replace(File.separator, "/")
        if(packages.length > 1)
            packages = packages.drop(1) + "/"
        
        val fileNameParts = file.getName().split(SEPARATOR)
        if (fileNameParts.size >= 3) {

        } else {
            throw new ConvertFileToEventException
        }
        val resolvedName = packages + fileNameParts.drop(2).mkString
        //fileNameParts.splitAt(2)._2.mkString
        new Event(fileNameParts(1), fileNameParts(0).toLong, resolvedName, file)
    }

}
class ConvertFileToEventException extends RuntimeException
