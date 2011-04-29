package sae
package operators

case class Sum[K <: AnyRef, V <: AnyRef](val source: Observable[K], val f: K => Int, val f2: (K, Int) => V) extends Observer[K] with Observable[V] {

  private var totalSum: Int = Integer.MIN_VALUE

  source.addObserver(this)

  def updated(oldV: K, newV: K) {
    val ns = totalSum - f(oldV) + f(newV)
    element_updated(f2(oldV, totalSum), f2(newV, ns))
    totalSum = ns
  }

  def removed(k: K) {
    if (totalSum - f(k) > 0)
      element_updated(f2(k, totalSum), f2(k, totalSum - f(k)))
    else
      element_removed(f2(k, totalSum))
    totalSum -= f(k)
  }
//unterscheiden zwischen observal value und observal "set"
  def added(k: K) {
    if (totalSum == Integer.MIN_VALUE) {
      element_added(f2(k, f(k)))
      totalSum = f(k)
    } else {

      element_updated(f2(k, totalSum), f2(k, totalSum + f(k)))
      totalSum += f(k)
    }

  }

}