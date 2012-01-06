package unisson.model.debug

import sae.Observer

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.01.12
 * Time: 11:10
 *
 */
class PrintingObserver[-V <: AnyRef] extends Observer[V]{
    def updated(oldV: V, newV: V) {println(oldV + "->" + newV)}

    def removed(v: V) {println("-" + v)}

    def added(v: V) {println("+" + v)}
}