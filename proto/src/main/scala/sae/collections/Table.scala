package sae
package collections

/**
 * A table stores relations in a database
 */
class Table[V <: AnyRef]
        extends Bag[V]
{
    initialized = true

	def lazyInitialize : Unit = { /* nothing to do, the table itself is the data */ }
   
    def copy : Table[V] =
        {
            val copy = new Table[V]()
            this.foreach(e =>
                {
                    copy.add_element(e)
                }
            )
            copy
        }
}