package sae.profiler.util
import sae._
import scala.collection.mutable.ListBuffer

class ObserverBuffer[V <: AnyRef](val source : LazyView[V]) extends Observer[V] with LazyView[V] {
    source.addObserver(this)
    var buffer = ListBuffer[(V,Option[V], String)]()
    def reset() {
        buffer = ListBuffer[(V,Option[V], String)]()
    }
    
	def updated(oldV : V, newV : V) : Unit ={
        buffer += new Tuple3(oldV, Some(newV),"updated")
    }

    def removed(v : V) : Unit = {
        buffer += new Tuple3(v,None, "removed")
    }

    def added(v : V) : Unit = {



        buffer += new Tuple3(v,None, "added")
    }
    
    def replay() : Unit = {
        buffer.foreach(x => 
            {
                x match {
                    case (v, _ , "removed") => element_removed(v)
                    case (v, _ , "added") => element_added(v)
                    case (v, Some(v2), "updated") => element_updated(v,v2)
                }
            })
        
    }  
    def lazy_foreach[T](f : (V) => T) {
        //source.lazy_foreach(f)
      buffer.foreach(x => f(x._1))
    }
    def lazyInitialize : Unit = {
        //source.lazyInitialize
    }
 
}