package sae
package collections

/**
 * An index backed by an immutable scala hash map
 */
/*
class HashMap[K <: AnyRef, V <: AnyRef](
    val relation : MaterializedView[V],
    val keyFunction : V => K)
        extends Collection[(K, V)]
        with Index[K, V] {

    private var map = new scala.collection.immutable.HashMap[K, V]

    relation addObserver this
    
    protected def put_internal(key : K, value : V) : Unit =
        map += (key -> value)

    protected def get_internal(key : K) : Option[V] =
        map.get(key)

    protected def isDefinedAt_internal(key : K) : Boolean =
        map.isDefinedAt(key)

    def materialized_foreach[U](f : ((K, V)) => U) : Unit =
        map.foreach(f);

    def materialized_size : Int =
        map.size

    def materialized_singletonValue : Option[(K, V)] =
        {
            if (size != 1)
                None
            else
                Some(map.first)
        }
    
    def add_element(kv : (K, V)) : Unit =
        {
    		map += (kv._1 -> kv._2)
        }

    def remove_element(kv : (K, V)) : Unit =
        {
    		map -= (kv._1)
        }
    
    def updated(oldV : V, newV : V) : Unit =
    {
        if( oldV == newV )
            return
        val k1 = keyFunction(oldV)
        val k2 = keyFunction(newV)
        if( k1 == k2 )
            map = map.updated(k1, newV)
    }

    def removed(v : V) : Unit =
    {
        val k = keyFunction(v)
        map -= (k)
    }

    def added(v : V) : Unit = {
        val k = keyFunction(v)
        map += (k -> v)        
    }
}
*/