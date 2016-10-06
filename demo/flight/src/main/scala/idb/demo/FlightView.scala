package idb.demo

import java.util.{Calendar, Date}


import akka.actor._
import idb.observer.{CountingObserver, Observable}
import idb.operators.impl._
import idb.remote._
import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.{MaterializedView, SetTable}
import idb.syntax.iql.impl._
import idb.syntax.iql.planning.PlanPrinter


/**
 * Sealed trait to simplify pickling.
 */
sealed trait HostMessage
case class HostObservableAndForward[T](obs: Observable[T], target: ActorRef)/*(implicit pickler: Pickler[T])*/ extends HostMessage
case class DoIt[T](fun: Observable[T] => Unit) extends HostMessage

class ObservableHost[T] extends Actor {
  var hosted: Option[Observable[T]] = scala.None

  override def receive = {
    case HostObservableAndForward(obs, target) =>
      obs.addObserver(new SentToRemote(target))
      hosted = scala.Some(obs)

    case DoIt(fun: (Observable[T] => Unit)) =>
      fun(hosted.get)
  }
}

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
      f.takeoff >= new Date(2014, 1, 1) AND
      f.takeoff < new Date(2015, 1, 1))
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

    for (d <- 1 to 360)
      flight += Flight(1, 5, new Date(2015,  1,  d, 10,  0))
  }


  def showResult[V](name: String, res: MaterializedView[V]): Unit = {
    Predef.println(s"Results $name")
    res.foreach(s => Predef.println("  " + s))
  }

  def main(args: Array[String]): Unit = {
    import Predef.println

    val system = ActorSystem("Flight")

    val flightHost = system.actorOf(Props[ObservableHost[Flight]])
    // we can later add flights by doing:
    // flightHost ! DoIt(obs => obs.asInstanceOf[SelectionView[_]].relation += Flight(3, 5, new Date(2014,  9, 14, 20, 15))

    val airportHost = system.actorOf(Props[ObservableHost[Airport]])


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

                // this equi join joins two remote partitions, but the join itself is local to the client
                val remoteJoin = EquiJoinView(airportRemote, flightRemote, ix1, ix2, fProjEqui, isSetEqui)

                // need a local actor which can receive msgs from the remote host -> flightRemote.actorRef
                // put `flightPartition` onto `flightHost` actor
                flightHost ! HostObservableAndForward(flightPartition, flightRemote.actorRef)
                airportHost ! HostObservableAndForward(airportPartition, airportRemote.actorRef)

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