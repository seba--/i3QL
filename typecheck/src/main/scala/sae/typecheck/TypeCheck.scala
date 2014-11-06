package sae.typecheck

import TypeStuff._

trait TypeCheck {
  def typecheck(e: Exp): Either[Type, TError]
  def typecheckIncremental(e: Exp): Either[Type, TError]

  def printTypecheck(e: Exp) = println(s"Type of $e is ${typecheck(e)}")
  def printTypecheck(name: String, e: Exp) = println(s"Type of $name is ${typecheck(e)}")
}