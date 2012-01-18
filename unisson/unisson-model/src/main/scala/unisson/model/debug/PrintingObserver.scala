package unisson.model.debug

import sae.Observer

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.01.12
 * Time: 11:10
 *
 */
class PrintingObserver[-V <: AnyRef](val prefix:String) extends Observer[V]{

    def this(){
        this("")
    }

    def updated(oldV: V, newV: V) {println(prefix + oldV + "->" + newV)}

    def removed(v: V) {println(prefix + "-" + v)}

    def added(v: V) {println(prefix + "+" + v)}
}