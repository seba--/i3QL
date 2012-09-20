package sae
package collections

import com.google.common.collect.Sets.SetView
import capabilities.{SingletonValue, Size}

/**
 * A result is a kind of view that offers more convenience operators
 * for working with the underlying data.
 * The result does not need to store all data internally and is thus not a
 * materialized view. In particular if a result is required from a
 * materialized view, a simple Proxy is used.
 */
trait QueryResult[V <: AnyRef]
        extends MaterializedView[V]
        with Size
        with SingletonValue[V]
        with Listable[V]
{

}

/**
 * A result that materializes all data from the underlying relation into a bag
 */
class BagResult[V <: AnyRef](
                                    val relation: Relation[V]
                                    )
        extends QueryResult[V]
        with Bag[V]
        with Observer[V]
{

    relation addObserver this

    override protected def children = List(relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(this)
        }
        Nil
    }

    def lazyInitialize {
        if (initialized) return
        relation.lazy_foreach(
            v =>
                add_element(v)
        )
        initialized = true
    }

    def updated(oldV: V, newV: V) {
        if (!initialized) {
            initialized = true
        }
        update(oldV, newV)
    }

    def removed(v: V) {
        if (!initialized) {
            initialized = true
        }

        this -= v
    }

    def added(v: V) {
        if (!initialized) {
            initialized = true
        }
        this += v
    }

    // def toAst = "QueryResult( " + relation.toAst + " )"
}


/**
 * A result that materializes all data from the underlying relation into a set
 */
class SetResult[V <: AnyRef](
                                val relation: SetRelation[V]
                                )
    extends QueryResult[V]
    with HashSet[V]
    with Observer[V]
{

    relation addObserver this

    override protected def children = List(relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(this)
        }
        Nil
    }

    def lazyInitialize() {
        if (initialized) return
        initialized = true
    }

    def updated(oldV: V, newV: V) {
        if (!initialized) {
            initialized = true
        }
        this -= oldV
        this += newV
    }

    def removed(v: V) {
        if (!initialized) {
            initialized = true
        }

        this -= v
    }

    def added(v: V) {
        if (!initialized) {
            initialized = true
        }
        this += v
    }

}


/**
 * A result that uses the underlying relation knowing that it is already
 * materialized.
 */
class MaterializedViewProxyResult[V <: AnyRef](
                                                      val relation: MaterializedView[V]
                                                      )
        extends QueryResult[V]
        with SelfMaintainedView[V, V]
{

    relation.addObserver(this)

    override protected def children = List(relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(this)
        }
        Nil
    }

    def lazyInitialize {
        // the relation will initialize itself on calls to the materialized_* methods
    }

    protected def materialized_contains(v: V) = relation.contains(v)

    protected def materialized_singletonValue = relation.singletonValue

    protected def materialized_size = relation.size

    protected def materialized_foreach[T](f: (V) => T) {
        relation.foreach(f)
    }

    // def toAst = "QueryResult( " + relation.toAst + " )"

    def updated_internal(oldV: V, newV: V) {
        element_updated(oldV, newV)
    }

    def added_internal(v: V) {
        element_added(v)
    }

    def removed_internal(v: V) {
        element_removed(v)
    }

}

class EmptyResult[V <: AnyRef] extends QueryResult[V]
{
    def lazyInitialize {}

    protected def materialized_foreach[T](f: (V) => T) {}

    protected def materialized_size: Int = 0

    protected def materialized_singletonValue: Option[V] = None

    protected def materialized_contains(v: V): Boolean = false
}