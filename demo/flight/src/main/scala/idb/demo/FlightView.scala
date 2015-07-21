package idb.demo

import java.util.{Calendar, Date}


import akka.actor.ActorSystem
import idb.observer.CountingObserver
import idb.operators.impl._
import idb.remote._
import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.{MaterializedView, SetTable}
import idb.syntax.iql.impl._
import idb.syntax.iql.planning.PlanPrinter

object FlightView {

  val airport = SetTable.empty[Airport]
  val flight = SetTable.empty[Flight]

  val q = (
    SELECT ((s: Rep[String]) => s,
            COUNT(*))
    FROM (airport, airport, flight)
    WHERE ((a1, a2, f) =>
      f.from == a1.id AND
      f.to == a2.id AND
      a2.code == "PDX" AND
      f.takeoff >= new Date(2014, 01, 01) AND
      f.takeoff < new Date(2015, 01, 01))
    GROUP BY ((a1: Rep[Airport], a2: Rep[Airport], f: Rep[Flight]) => a1.city)
  )

  val compiledQuery: Relation[(String, Int)] = q

  def initAirports(): Unit = {
    airport += Airport(1, "AMS", "Amsterdam")
    airport += Airport(2, "BOS", "Boston")
    airport += Airport(3, "SFO", "San Francisco")
    airport += Airport(4, "NRT", "Tokyo")
    airport += Airport(5, "PDX", "Portland")
  }


  def initFlights(): Unit = {
    for (d <- 1 to 360)
      flight += Flight(1, 5, new Date(2014,  1,  d, 10,  0))

    for (d <- 1 to 349)
      flight += Flight(2, 5, new Date(2014, 1, d, 17,  5))
    flight += Flight(2, 5, new Date(2014, 12, 31, 17,  5))

    for (d <- 1 to 3467)
      flight += Flight(3, 5, new Date(2014, 1, 1, d,  0))

    for (d <- 1 to 349)
      flight += Flight(4, 5, new Date(2014, 1, d, 16,  0))
  }


  def showResult[V](name: String, res: MaterializedView[V]): Unit = {
    Predef.println(s"Results $name")
    res.foreach(s => Predef.println("  " + s))
  }

  def main(args: Array[String]): Unit = {
    import Predef.println

    val system = ActorSystem("Flight")

    val compiledClientCounts = new CountingObserver
    val partitionedClientCounts = new CountingObserver

    val partitionedQuery = compiledQuery match {
      case AggregationForSelfMaintainableFunctions(relAgg, fGroup, fAggFact, fConvert, isSetAgg) =>
        val partitionedRelAgg = relAgg match {
          case ProjectionView(relProj, fProj, isSetProj) =>
            val partitionedRelProj = relProj match {
              case EquiJoinView(airportPartition, flightPartition, ix1, ix2, fProjEqui, isSetEqui) =>
                val airportRemote = RemoteView(airportPartition, system)
                val flightRemote = RemoteView(flightPartition, system)
                val remoteJoin = EquiJoinView(airportRemote, flightRemote, ix1, ix2, fProjEqui, isSetEqui)

                airportRemote addObserver partitionedClientCounts
                flightRemote addObserver partitionedClientCounts

                airportPartition.asInstanceOf[CrossProductView[_,_,_]].left addObserver compiledClientCounts
                airportPartition.asInstanceOf[CrossProductView[_,_,_]].right.asInstanceOf[SelectionView[_]].relation addObserver compiledClientCounts
                flightPartition.asInstanceOf[SelectionView[_]].relation addObserver compiledClientCounts

                remoteJoin
            }

            ProjectionView(partitionedRelProj, fProj, isSetProj)
        }

        AggregationForSelfMaintainableFunctions(partitionedRelAgg, fGroup, fAggFact, fConvert, isSetAgg)
    }

    println(s"Compiled query:\n${compiledQuery.prettyprint("  ")}")
    println(s"Partitioned query:\n${partitionedQuery.prettyprint("  ")}")

    val result = compiledQuery.asMaterialized
    val parresult = partitionedQuery.asMaterialized

    showResult("result", result)
    showResult("parresult", parresult)
    println()

    initAirports()
    initFlights()

    println("Initial flights:")
    showResult("result", result)
    showResult("parresult", parresult)
    println()

    flight += Flight(3, 5, new Date(2014,  9, 14, 20, 15))
    println("Updated flights:")
    showResult("result", result)
    showResult("parresult", parresult)
    println()

    flight ~= Flight(2, 5, new Date(2014, 12, 31, 17,  5)) -> Flight(2, 5, new Date(2015,  1,  1, 11,  5))
    println("Updated flights (2):")
    showResult("result", result)
    showResult("parresult", parresult)
    println()

    println(s"Messages reaching client in compiled query: ${compiledClientCounts.msgCount}")
    println(s"Data packages reaching client in compiled query: ${compiledClientCounts.dataCount}")
    println(s"Messages reaching client in partitioned query: ${partitionedClientCounts.msgCount}")
    println(s"Data packages reaching client in partitioned query: ${partitionedClientCounts.dataCount}")

    system.shutdown()
  }
}