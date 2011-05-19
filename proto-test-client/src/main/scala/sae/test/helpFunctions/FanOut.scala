package sae.test.helpFunctions
import sae.operators._
import sae.functions._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import de.tud.cs.st.bat._
import scala.collection.mutable.Set
private class FanOutIntern[Domain <: AnyRef](val f : Domain => (Seq[Type], Type)) extends AggregationFunktion[Domain, Set[String]] {

    import com.google.common.collect._;
    val dep = HashMultiset.create[String]()

    var value : Option[Domain] = None
    def add(d : Domain, data : Iterable[Domain]) = {
        dep.add(f(d)._2.toJava)
        f(d)._1.foreach(x => dep.add(x.toJava))
        dep.elementSet()
    }
    def remove(d : Domain, data : Iterable[Domain]) = {
        dep.remove(f(d)._2.toJava)
        f(d)._1.foreach(x => dep.remove(x.toJava))
        dep.elementSet()
    }

    def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
        dep.add(f(newV)._2.toJava)
        f(newV)._1.foreach(x => dep.add(x.toJava))
        dep.add(f(oldV)._2.toJava)
        f(oldV)._1.foreach(x => dep.add(x.toJava))
        dep.elementSet()
    }
}

object FanOut {
    def apply[Domain <: AnyRef](f : (Domain => (Seq[Type], Type))) = {
        new AggregationFunktionFactory[Domain, Set[String]] {
            def apply() : AggregationFunktion[Domain, Set[String]] = {
                new FanOutIntern[Domain](f)
            }
        }
    }
}