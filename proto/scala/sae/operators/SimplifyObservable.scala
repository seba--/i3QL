package sae.operators
import sae._
class SimplifyObservable[K <: AnyRef, V <: AnyRef]  extends Observer[(K, Observable[V])] with Observable[V] {
    val obs = new Observer[V] {
       def updated(oldV: V, newV: V): Unit = {
         element_updated(oldV, newV)
       }

       def removed(v: V): Unit = {
         element_removed(v)
       }

       def added(v: V): Unit = {
         element_added(v)
       }
    }
    def updated(oldV: (K, Observable[V]), newV: (K, Observable[V])): Unit = {
    	removed(oldV)
    	added(newV)
    }

    def removed(v: (K, Observable[V])): Unit = {
      v._2.removeObserver(obs)
      
    }

    def added(v: (K, Observable[V])): Unit = {
      v._2.addObserver(obs)
    }
  }

