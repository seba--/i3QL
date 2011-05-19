package sae
package bytecode
package model

case class MethodCall(source : Method, target : Method, programCounterLocation : Int)