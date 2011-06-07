package sae.profiler.util
import java.io.File
import de.tud.cs.st.lyrebird.replayframework._
import sae.bytecode.MaterializedDatabase
import sae.bytecode.BytecodeDatabase

class ReplayHelper(val location : File) {
    private val db = new BytecodeDatabase()
	private val replay = new Replay(location)
	private val allEventSets = replay.getAllEventSets()
	private var idx = 0
	val buffer  = new  DatabaseBuffer(db)

	
	def applyNext() : Unit = {
	    buffer.reset
	    if(idx < allEventSets.size){
	        replay.processEventSet(allEventSets(idx), db.getAddClassFileFunction,db.getRemoveClassFileFunction)
	        idx += 1
	    }else{
	        throw new Error()
	    }  
	}

	def hasNext(): Boolean = {
	    idx < allEventSets.size
	}
	
	def applyAll(text : List[String]){
	    
	    var i = 0
	    while(this.hasNext){
	        if(i < text.size){
	            applyNext
	            
	        	Write(text(i),Profile(buffer.replay))
	        }else{
	            applyNext
	           
	            Write("Profiling EventSet Number: " + i, Profile(buffer.replay))
	        }
	        i +=1
	    }
	}
	def applyAll() {
	    applyAll(List[String]())
	}
	def getBuffer() : DatabaseBuffer = {
	    buffer
	}
}