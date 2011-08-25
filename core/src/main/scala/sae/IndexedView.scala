package sae

/**
 * This trait represents a materialized view that can be indexed
 * for faster joins, etc.
 */
trait IndexedView[V <: AnyRef]
        extends MaterializedView[V] {

    // internally we forget the concrete Key type, since there may be many 
    // different keys
    // TODO Manifest Types can help here
    var indices = new scala.collection.immutable.HashMap[(V => _), Index[_,V]] 

    // Indices MUST be updated prior to any other notifications
    override def element_added(v: V) {
        indices.values.foreach( _.added(v) )
        super.element_added(v)
    }

    override def element_removed(v: V) {
        indices.values.foreach( _.removed(v) )
        super.element_removed(v)
    }

    override def element_updated(oldV: V, newV: V) {
        indices.values.foreach( _.updated(oldV, newV) )
        super.element_updated(oldV, newV)
    }


    /**
     * returns an index for specified key function
     */
    def index[K <: AnyRef](keyFunction : V => K) : Index[K, V] = {
        val index = indices.getOrElse(
                keyFunction,
                {
                    val newIndex = createIndex(keyFunction)
                    indices += (keyFunction -> newIndex)
                    newIndex
                }
        )
        index.asInstanceOf[Index[K,V]]
    }
        
    protected def createIndex[K <: AnyRef](keyFunction : V => K) : Index[K, V]
}