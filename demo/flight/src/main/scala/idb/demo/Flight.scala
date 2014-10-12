package idb.demo

import java.util.{Calendar, Date}


import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.SetTable

object Flight {

  type _Airport = (Int, String, String)
  type Airport = Rep[_Airport]
  object Airport {
    def apply(id: Int, code: String, city: String) = (id, code, city)
    def unapply(a: _Airport) = Some(a)
  }
  implicit def airportOps(a: Airport) = AirportOps(a)
  case class AirportOps(a: Airport) {
    def id = a._1
    def code = a._2
    def city = a._3
  }

  type _Flight = (Int, Int, Date)
  type Flight = Rep[_Flight]
  object Flight {
    def apply(from: Int, to: Int, takeoff: Date) = (from, to, takeoff)
    def unapply(a: _Flight) = Some(a)
  }
  implicit def flightOps(a: Flight) = FlightOps(a)
  case class FlightOps(a: Flight) {
    def from = a._1
    def to = a._2
    def takeoff = a._3
  }

  val airport = SetTable.empty[_Airport]
  val flight = SetTable.empty[_Flight]


  val q = (
    SELECT ((s: Rep[String]) => s, COUNT(*))
    FROM (airport, airport, flight)
    WHERE ((a1: Airport, a2: Airport, f: Flight) =>
      f.from == a1.id &&
      f.to == a2.id &&
      a2.code == "PDX" &&
      f.takeoff >= new Date(2014, 01, 01) &&
      f.takeoff < new Date(2015, 01, 01))
    GROUP BY ((a1: Airport, a2: Airport, f: Flight) => a1.city)
  )


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


  def main(args: Array[String]): Unit = {
    import Predef.println

    val result = q.asMaterialized
    result.foreach(println(_))


    initAirports()
    initFlights()

    println("Initial flights:")
    result.foreach(println(_))

    println()
    flight += Flight(3, 5, new Date(2014,  9, 14, 20, 15))
    println("Updated flights:")
    result.foreach(println(_))

    println()
    flight ~= Flight(2, 5, new Date(2014, 12, 31, 17,  5)) -> Flight(2, 5, new Date(2015,  1,  1, 11,  5))
    println("Updated flights (2):")
    result.foreach(println(_))
  }
}