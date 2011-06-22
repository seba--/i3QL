package sae.operators.intern

/**
 * Date: 20.06.11
 * Time: 18:15
 * @author Malte V
 */

protected class Count {
    private var count : Int = 0
    def inc() = { this.count += 1 }
    def dec() : Int = { this.count -= 1; this.count }
    def apply() = this.count
}