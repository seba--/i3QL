package queens

import saere._

// ONLY TO BE USED BY GOALS THAT WILL NEVER INSTANTIATE TERMS
trait Once extends Solutions {
	
	def eval() : Boolean
	
	private var called = false
	
	final def next() : Boolean = {
		if (!called) {
			called = true
			eval
		} else {
			false
		}
	}
	
}
