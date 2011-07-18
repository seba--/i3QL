package sae.functions

import sae.operators._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import de.tud.cs.st.bat._
import scala.collection.mutable.Set


private class FanOutIntern[Domain <: AnyRef](val f : Domain => (Seq[Type], Type), val select : Type => Boolean) extends SelfMaintainalbeAggregateFunction[Domain, Set[String]] {

    import com.google.common.collect._;
    val dep = HashMultiset.create[String]()

    var value : Option[Domain] = None
    def add(d : Domain) = {
        if (select(f(d)._2))
            dep.add(f(d)._2.toJava)
        f(d)._1.foreach(x => {
            val t = x.toJava
            if (select(x)) 
            dep.add(x.toJava)
            })
            dep.elementSet().clone()
    }
    
    def remove(d : Domain) = {
        if (select(f(d)._2))
            dep.remove(f(d)._2.toJava)
        f(d)._1.foreach(x => if (select(x)) dep.remove(x.toJava))
        dep.elementSet().clone()
    }
    
    def update(oldV : Domain, newV : Domain) = {
        if (select(f(oldV)._2))
            dep.remove(f(oldV)._2.toJava)
        f(oldV)._1.foreach(x => if (select(x)) dep.remove(x.toJava))
        if (select(f(newV)._2))
            dep.add(f(newV)._2.toJava)
        f(newV)._1.foreach(x => if (select(x)) dep.add(x.toJava))
        dep.elementSet().clone()
    }
}

object FanOut {
    def apply[Domain <: AnyRef](f : (Domain => (Seq[Type], Type)), select : Type => Boolean) = {
        new SelfMaintainalbeAggregateFunctionFactory[Domain, Set[String]] {
            def apply() : SelfMaintainalbeAggregateFunction[Domain, Set[String]] = {
                new FanOutIntern[Domain](f, select)
            }
        }
    }
}