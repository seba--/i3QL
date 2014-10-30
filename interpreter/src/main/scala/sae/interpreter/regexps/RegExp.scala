package sae.interpreter.regexps

/**
 * @author Mirko Köhler
 */
trait RegExp
case class Terminal(s: String) extends RegExp
case class Alt(r1: RegExp, r2: RegExp) extends RegExp
case class Asterisk(r1: RegExp) extends RegExp
case class Sequence(r1: RegExp, r2: RegExp) extends RegExp
