package unisson.model.debug

import sae.Observer
import sae.deltas.{Update, Deletion, Addition}

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.01.12
 * Time: 11:10
 *
 */
class PrintingObserver[-V <: AnyRef](val prefix:String = "") extends Observer[V]{

    def updated(oldV: V, newV: V) {println(prefix + oldV + "->" + newV)}

    def removed(v: V) {println(prefix + "-" + v)}

    def added(v: V) {println(prefix + "+" + v)}

    def updated[U <: V](update: Update[U]) {}

    def modified[U <: V](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {}
}