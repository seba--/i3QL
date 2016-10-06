package idb.lms.extensions

import java.util.Date

import idb.lms.extensions.equivalence.BaseExpAlphaEquivalence

import scala.language.implicitConversions
import scala.reflect.SourceContext
import scala.virtualization.lms.common.{Base, OrderingOpsExp, ScalaGenBase}

/**
 * Created by seba on 11/10/14.
 */
trait FlightOps extends Base {

  case class Airport(id: Int, code: String, city: String)
  case class Flight(from: Int, to: Int, takeoff: Date)
  
  implicit def repAirportToAirportOps(a: Rep[Airport]) = new airportOpsCls(a)
  implicit def repFlightToFlightOps(f: Rep[Flight]) = new flightOpsCls(f)

  class airportOpsCls(a: Rep[Airport]) {
    def id = airport_id(a)
    def code = airport_code(a)
    def city = airport_city(a)
  }
  class flightOpsCls(f: Rep[Flight]) {
    def from = flight_from(f)
    def to = flight_to(f)
    def takeoff = flight_takeoff(f)
  }

  def airport_id(a: Rep[Airport])(implicit pos: SourceContext): Rep[Int]
  def airport_code(a: Rep[Airport])(implicit pos: SourceContext): Rep[String]
  def airport_city(a: Rep[Airport])(implicit pos: SourceContext): Rep[String]
  def flight_from(f: Rep[Flight])(implicit pos: SourceContext): Rep[Int]
  def flight_to(f: Rep[Flight])(implicit pos: SourceContext): Rep[Int]
  def flight_takeoff(f: Rep[Flight])(implicit pos: SourceContext): Rep[Date]
}

trait FlightOpsExp extends FlightOps with BaseExpAlphaEquivalence with OrderingOpsExp {

  case class AirportId(a: Exp[Airport]) extends Def[Int]
  case class AirportCode(a: Exp[Airport]) extends Def[String]
  case class AirportCity(a: Exp[Airport]) extends Def[String]
  case class FlightFrom(f: Exp[Flight]) extends Def[Int]
  case class FlightTo(f: Exp[Flight]) extends Def[Int]
  case class FlightTakeoff(f: Exp[Flight]) extends Def[Date]

  def airport_id(a: Rep[Airport])(implicit pos: SourceContext) = AirportId(a)
  def airport_code(a: Rep[Airport])(implicit pos: SourceContext) = AirportCode(a)
  def airport_city(a: Rep[Airport])(implicit pos: SourceContext) = AirportCity(a)
  def flight_from(f: Rep[Flight])(implicit pos: SourceContext) = FlightFrom(f)
  def flight_to(f: Rep[Flight])(implicit pos: SourceContext) = FlightTo(f)
  def flight_takeoff(f: Rep[Flight])(implicit pos: SourceContext) = FlightTakeoff(f)

  override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
    (a, b) match {
      case (AirportId(a1), AirportId(a2)) => isEquivalent(a1, a2)
      case (AirportCode(a1), AirportCode(a2)) => isEquivalent(a1, a2)
      case (AirportCity(a1), AirportCity(a2)) => isEquivalent(a1, a2)
      case (FlightFrom(f1), FlightFrom(f2)) => isEquivalent(f1, f2)
      case (FlightTo(f1), FlightTo(f2)) => isEquivalent(f1, f2)
      case (FlightTakeoff(f1), FlightTakeoff(f2)) => isEquivalent(f1, f2)
      case _ => super.isEquivalentDef(a, b)
    }

  override def mirror[A:Manifest](e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
    case AirportId(a) => airport_id(f(a))
    case AirportCode(a) => airport_code(f(a))
    case AirportCity(a) => airport_city(f(a))
    case FlightFrom(fl) => flight_from(f(fl))
    case FlightTo(fl) => flight_to(f(fl))
    case FlightTakeoff(fl) => flight_takeoff(f(fl))
    case _ => super.mirror(e,f)
  }).asInstanceOf[Exp[A]]

}

trait ScalaGenFlightOps extends ScalaGenBase {
  val IR: FlightOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case AirportId(a) => emitValDef(sym, src"$a.id")
    case AirportCode(a) => emitValDef(sym, src"$a.code")
    case AirportCity(a) => emitValDef(sym, src"$a.city")
    case FlightFrom(f) => emitValDef(sym, src"$f.from")
    case FlightTo(f) => emitValDef(sym, src"$f.to")
    case FlightTakeoff(f) => emitValDef(sym, src"$f.takeoff")
    case _ => super.emitNode(sym, rhs)
  }
}