package sae
package operators

/**
 * A join ....
 */
class HashEquiJoin[A <: AnyRef, B <: AnyRef, Z <: AnyRef, K <: AnyRef](
        val left : View[A],
        val right : View[B],
        val leftKey : A => K,
        val rightKey : B => K,
        val joinFunction : (A, B) => Z) //	 extends Bag[(A,B)]
        //		with LazyInitializer[(A,B)]
        {

}