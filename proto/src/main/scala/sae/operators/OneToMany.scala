package sae.operators

import sae._

trait OneToMany[Domain <: AnyRef,Result <: AnyRef]
        extends LazyView[Result] {
	val oneToMany : Domain => List[Result]
}

class DefaultOneToMany[Domain <: AnyRef,Result <: AnyRef](
    val source : LazyView[Domain],
    val oneToMany : Domain => List[Result]     
	) extends OneToMany[Domain,Result]
	                    with Observer[Domain]{
    
    source.addObserver(this)
   
    def lazy_foreach[T](f : (Result) => T) {
        source.lazy_foreach(x => oneToMany(x).foreach(f))
    }


    def lazyInitialize : Unit = {
        source.lazyInitialize
    }
    def updated(oldV : Domain, newV : Domain) : Unit ={
        oneToMany(oldV).foreach(x => element_removed(x))
        oneToMany(newV).foreach(x => element_added(x))
    }

    def removed(v : Domain) : Unit={
        oneToMany(v).foreach(x => element_removed(x))
    }

    def added(v : Domain) : Unit={
        oneToMany(v).foreach(x => element_added(x))
    }
}
