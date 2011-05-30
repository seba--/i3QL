package sae.reader
import java.io.File

import scala.collection.immutable.SortedMap
import scala.collection.mutable.Map

/**
 * case class for saving one change/add/remove event
 * previousEvent: saves the previous event to a classFile if ByteCodeTracker recorded an event before the current Event for the classfile
 * IMPORTENT: it is possible to get a remove event WITHOUT a previous add / change Event. (e.g. project clean generates the events: remove and then add)
 */
case class Event(val eventType : String, val eventTime : Long, val resolvedClassName : String, val eventFile : File, val previousEvent : Option[Event]) {
    // resolvedClassName := package/subpackage/ClassName
    def getCorrespondingEvent() : Option[Event] = {
        previousEvent
    }
}
object Event {
    val CHANGED = "CHANGED"
    val REMOVED = "REMOVED"
    val ADDED = "ADDED"
}
case class EventSet(val eventFiles : List[Event]) {

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
     private var previousEvents = Map[String, Event]()
	/**
	 * IMPORTANT every method call will reprocess the whole directory
	 */
    def foreach(f : EventSet => _) : Unit = {
        getAllFilesGroupedByEventTime(location).foreach(x => f(eventsToEventSet(x)))
    }
    /**
     * returns a list with all EventSets 
     * @return : list with all EventSets in the given location (constructor)
     * IMPORTANT every method call will reprocess the whole directory
     */
    def getAllEventSets() : List[EventSet] = {
        var res = List[EventSet]()
        getAllFilesGroupedByEventTime(location).foreach( x => res = eventsToEventSet(x) :: res)
        res
    }
    
    /**
     * converts a list of Events into one EventSet
     */
    private def eventsToEventSet(eventSet : List[Event]) : EventSet = {
        new EventSet(eventSet)
    }

    /**
     * method that returns all files in a dir and all sub dirs wrapped in Events,
     * grouped by the event time as a List of Lists (one list for every real event [real events can contain
     * more then one Event])
     * ONLY PUBLIY FOR TESTING
     */
    def getAllFilesGroupedByEventTime(currentLocation : File) = {
        previousEvents = Map[String, Event]()
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
        subList :: list

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
        //should normally only used on a dir that was created by ClassfileChangeTracker
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
        if (packages.length > 1)
            packages = packages.drop(1) + "/"

        val fileNameParts = file.getName().split(SEPARATOR)
        if (fileNameParts.size >= 3) {

        } else {
            throw new ConvertFileToEventException
        }
        val resolvedName = packages + fileNameParts.drop(2).mkString.dropRight(6)
        val previousEvent = previousEvents.get(resolvedName)
        val res = previousEvent match {
            case Some(x) => new Event(fileNameParts(1), fileNameParts(0).toLong, resolvedName, file, Some(x))
            case _       => new Event(fileNameParts(1), fileNameParts(0).toLong, resolvedName, file, None)
        }
        previousEvents.put(resolvedName, res)
        res

    }

}
//file can not be converted into an event
class ConvertFileToEventException extends RuntimeException
