package sae.core.impl


import sae.core.MaterializedView


/**
 * A size cached maintained view, has counter for the size of the relation.
 * The counter is NOT automatically updated.
 * The counter requires explicit increments and decrements, through the incSize, decSize methods.
 * The size is checked for laziness as having a value of -1 if not initialized.
 * Clients of this class can either actively call initSize(Int) to provide a value or,
 * the value supplied by calling (initSize : Int) is used.
 */
trait SizeCachedMaintainedView
{
	def size : Int = 
	{
		if( size_data == -1 )
			size_data = initSize
		size_data
	}

	private var size_data = -1
	
	def initSize : Int
	
	def initSize(size:Int) : Unit = { this.size_data = size }
	
	def incSize : Unit = { size_data += 1}
	
	def decSize : Unit = { size_data -= 1}
}