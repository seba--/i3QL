package sae
package collections

/**
 * A result is a kind of view that offers more convenience operators
 * for working with the underlying data.
 * However, these operators need not be efficient
 */
trait Result[V <: AnyRef]
        extends View[V]
        with Size
        with SingletonValue[V]
        with Listable[V] {

}

/**
 * A result that materializes all data from the underlying relation into a bag
 */
class BagResult[V <: AnyRef](
    val relation : LazyView[V])
        extends Result[V]
        with Bag[V]
        with Observer[V] {

    relation addObserver this

    def lazyInitialize : Unit = {
        relation.lazy_foreach(v =>
            add_element(v)
        )
    }

    def updated(oldV : V, newV : V) : Unit =
        {
            this -= oldV
            this += newV
        }

    def removed(v : V) : Unit =
        {
            this -= v
        }

    def added(v : V) : Unit =
        {
            this += v
        }

}

/**
 * A result that uses the underlying relation knowing that it is already
 * materialized and just caches size and singletonValue attributes
 */
class MaterializedViewResult[V <: AnyRef](
    val relation : MaterializedView[V])
        extends MaterializedView[V]
        with Result[V]
        with Observer[V] {

    relation addObserver this

    var sizeData : Int = -1

    var singletonValueData : Option[V] = None

    def materialized_foreach[T](f : (V) => T) = relation.foreach(f)

    def lazyInitialize : Unit = {
        sizeData = 0
        relation.foreach(_ => sizeData += 1)
        if (sizeData == 1)
            relation.foreach(v => singletonValueData = Some(v))
    }

    override def size : Int = {
        if (!initialized) {
            lazyInitialize
            initialized = true
        }
        sizeData
    }

    override def singletonValue : Option[V] = {
        if (!initialized) {
            lazyInitialize
            initialized = true
        }
        singletonValueData
    }

    def updated(oldV : V, newV : V) : Unit =
        {
            // size does not change

            if (singletonValue == Some(oldV))
                singletonValueData = Some(newV)
        }

    def removed(v : V) : Unit =
        {
            sizeData -= 1
            if (singletonValue.isDefined)
                singletonValueData = None
        }

    def added(v : V) : Unit =
        {
            sizeData += 1
            if (singletonValue.isDefined) {
                singletonValueData = None
            } else {
                singletonValueData = Some(v)
            }
        }
}