package idb.operators.impl

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 14.07.13
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
class Count
{
	private var count: Int = 0

	def inc() {
		this.count += 1
	}

	def dec(): Int = {
		this.count -= 1
		this.count
	}

	def apply() = this.count
}
