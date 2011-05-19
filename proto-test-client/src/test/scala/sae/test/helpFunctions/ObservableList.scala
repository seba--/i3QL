package sae.test.helpFunctions
import sae.LazyView

class ObservableList[V <: AnyRef] extends LazyView[V] {
    def lazy_foreach[T](f : (V) => T) {
        foreach(f)
    }
    import com.google.common.collect.HashMultiset;
    val data : HashMultiset[V] = HashMultiset.create[V]()

    def lazyInitialize = {}
    
    def add(k : V) {
        data.add(k) // += k
        element_added(k)
    }

    def remove(k : V) {
        data.remove(k) //data -= k
        element_removed(k)
    }

    def update(oldV : V, newV : V) {
        data.remove(oldV)
        data.add(newV)
        element_updated(oldV, newV)
    }

    def foreach[T](f : (V) => T) {
        var x = data.iterator()
        while (x.hasNext()) {
            f(x.next)
            //x.next
        }
        //data.foreach(f)
    }

}