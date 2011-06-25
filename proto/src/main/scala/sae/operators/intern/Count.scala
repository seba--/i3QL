package sae.operators.intern

/**
 * Date: 25.06.11
 * Time: 13:18
 * @author Malte V
 */

protected class Count {
  private var count: Int = 0

  def inc() = {
    this.count += 1
  }

  def dec(): Int = {
    this.count -= 1; this.count
  }

  def apply() = this.count
}