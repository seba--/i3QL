package sae.playground.remote

import akka.actor.Address
import idb.{Relation, BagTable}
import idb.operators.impl.{EquiJoinView, SelectionView, ProjectionView}
import idb.remote.RemoteView
import org.scalatest.FunSuite

class Test extends FunSuite {

  def walk(rel: Relation[_], address: Option[Address] = None): Unit = {
    println(s"Walking ${rel.prettyprint("")}")
    rel match {
      case r: RemoteView[_] => {
        val addr = r.address
        println(s"descending: creating remote  (@ $addr)")
        walk(r.relation, addr)
        println("ascending, moving subtree")
      }
      case _ => rel.children.foreach { ch => walk(ch, address) }
    }
  }

  test("Correct recursive tree walking") {
    val db1 = BagTable.empty[Int]
    val db2 = BagTable.empty[Int]

    val root = RemoteView.createUnrealized(null/*TODO*/, new SelectionView[Int](
      new ProjectionView[(Int, Int), Int](
        EquiJoinView[Int, Int](
          new SelectionView(db1, (a: Int) => a % 2 == 0, false),
          new SelectionView(db2, (a: Int) => a % 3 == 0, false),
          Seq(x => x),
          Seq(x => x),
          false
        ),
        (pair: (Int, Int)) => pair._1 + pair._2,
        false
      ),
      _ => true,
      false
    )
    )

      walk(root)
  }
}
