package sae

/**
 * This view materializes its elements and thus requires
 * some form of intermediate storage.
 */
trait MaterializedView[V <: AnyRef]
        extends View[V] {

}