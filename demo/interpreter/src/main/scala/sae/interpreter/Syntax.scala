package sae.interpreter

/**
 * @author Mirko Köhler
 */

trait Syntax

//Arithmetic Syntax
trait ArithmeticSyntax
case object Plus extends ArithmeticSyntax
case object Abs extends ArithmeticSyntax
case object Max extends ArithmeticSyntax

//Boolean Syntax
trait BooleanSyntax
case object And extends BooleanSyntax

//Misc
case object Equals extends Syntax
case object IfThenElse extends Syntax









