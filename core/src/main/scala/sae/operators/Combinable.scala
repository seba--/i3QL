package sae.operators

/**
 * This trait is used to define classes which object's can be combined with each other. This trait is used to define the combination of two results in the data flow analysis.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:01
 */
trait Combinable[T] {

  /**
   * Combines this object with another object to create a new object.
   * @param other The object to be combined with.
   * @return A new object that is defined as the combination of this and the other object.
   */
  def combineWith(other : T) : T
}
