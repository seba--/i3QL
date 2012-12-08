package sae
package collections


/**
 * A relation that is guaranteed to hold each element only once
 */
trait Set[V]
    extends SetRelation[V]
    with Collection[V]
    with QueryResult[V]
{
    //private val data: scala.collection.mutable.HashSet[V] = new scala.collection.mutable.HashSet[V]()

    private var data: java.util.HashSet[V] = new java.util.HashSet[V]()

    import scala.collection.JavaConversions._

    def size: Int = data.size

    def singletonValue: Option[V] = data.headOption //None

    def add_element(v: V)
    {
        /*
        if (data.contains(v)){
            throw new IllegalStateException()
        }
        */
        //data = data + v
        data.add(v)
        data
    }

    def remove_element(v: V)
    {
        //data = data - v
        data.remove(v)
        data
    }

    def foreach[U](f: V => U) {
        data.foreach (f)
    }

    def foreachWithCount[T](f: (V, Int) => T) {
        data.foreach (v => f (v, 1))
    }

    def contains(v: V) = data.contains (v)

    def isDefinedAt(v: V) = {
        data.contains (v)
    }

    def elementCountAt[T >: V](v: T) = {
        if (v.isInstanceOf[V] && data.contains (v.asInstanceOf[V])) {
            1
        }
        else
        {
            0
        }
    }
}
