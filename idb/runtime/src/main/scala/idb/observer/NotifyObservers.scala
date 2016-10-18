/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.observer

/**
 * This trait defines the notify methods for working with observers.
 * The methods allow to work type correctly when notifying observers.
 * The incentive of the methods is that we know (statically)
 * that we can provide at least objects of type V.
 * All observers must accept the top type (Any), whether they
 * really work with the type V or not is up to the observers (and not the observables).
 *
 * @author Ralf Mitschke
 */
object NotifyObservers {
  var indent = 0
  def spaces = "| " * indent

  var DEBUG = false
}
import NotifyObservers._

trait NotifyObservers[V] {

  protected def observers: Iterable[Observer[Any]]

  protected def notify_added(v: V) {
    observers.foreach(obs =>
      printed(1, obs) {
        obs.added(v)
      }
    )
  }

  protected def notify_addedAll(vs: Seq[V]) {
    if (vs.isEmpty) {
    }
    else if (vs.size == 1) {
      val v = vs.head
      observers.foreach(obs =>
        printed(1, obs) {
          obs.added(v)
        }
      )
    }
    else {
      observers.foreach(obs =>
        printed(vs.size, obs) {
          obs.addedAll(vs)
        }
      )
    }
  }

  protected def notify_removed(v: V) {
    observers.foreach(_.removed(v))
  }

  protected def notify_removedAll(vs: Seq[V]) {
    if (vs.isEmpty) {
    }
    else if (vs.size == 1) {
      val v = vs.head
      observers.foreach(_.removed(v))
    }
    else
      observers.foreach(_.removedAll(vs))
  }

  protected def notify_updated(oldV: V, newV: V) {
    observers.foreach(_.updated(oldV, newV))
  }

  def printed[T](size: Int, obs: Any)(f: => T): T = {
    if (!DEBUG)
      f
    else {
      println(s"${spaces}size $size \t${this} -> \t${obs}")
      indent = indent + 1
      val t = f
      indent = indent - 1
      println(s"${spaces}size $size \t${this} <- \t${obs}")
      t
    }
  }

}
