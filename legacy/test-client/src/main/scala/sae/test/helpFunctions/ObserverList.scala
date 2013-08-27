package sae.test.helpFunctions
import scala.collection.mutable.ListBuffer
import sae.Observer

class ObserverList[Domain <: AnyRef] extends Observer[Domain] {
    val data = ListBuffer[Domain]()

    def contains(x : Any) : Boolean = {
        data.contains(x)
    }
    def size() : Int = {
        data.size
    }
    def updated(oldV : Domain, newV : Domain) {
        data -= oldV
        data += newV
    }

    def removed(v : Domain) {
        data -= v
    }

    def added(v : Domain) {
        data += v
    }
    override def toString() : String = {
        data.toString()
    }
}