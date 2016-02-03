package sae.playground.remote

import akka.actor.{ActorSystem, Props, Address}
import idb.remote.ObservableHost.Forward
import idb.{Relation, BagTable}
import idb.operators.impl.{EquiJoinView, SelectionView, ProjectionView}
import idb.remote.{RemoteViewActor, RemoteView}
import org.scalatest.FunSuite

class Test extends FunSuite {



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
