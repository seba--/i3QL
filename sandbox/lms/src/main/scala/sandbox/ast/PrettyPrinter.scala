package sandbox.ast

import sandbox.ast.sql.operators._

/**
 *
 * @author Ralf Mitschke
 */
object PrettyPrinter
{

  def apply (operator: Operator)(name: String = "graph"): String = {
    implicit val dot = new StringBuilder
    writeHeader (name)
    implicit val nodes = writeNodes (operator)
    writeEdges (operator)
    writeOpen
    writeClose

    dot.toString ()
  }


  private case class Node (name: String)
  {
    def this (num: Int) = {
      this ("node_" + num)
    }
  }

  private def writeNodes (operator: Operator)
      (implicit dot: StringBuilder, nodeMap: Map[Operator, Node] = Map.empty): Map[Operator, Node] =
  {
    val node = nodeMap.getOrElse (operator, new Node (nodeMap.size))
    operator match {
      case Selection (children, function) => {
        writeNode (node.name, "σ")
      }
      case TableReference (table) =>
        writeNode (node.name, table.name)
    }

    nodeMap.updated (operator, node)
  }

  private def writeEdges (operator: Operator)(implicit dot: StringBuilder) {
    operator match {
      case Selection (children, _) =>
        for (child <- children) {

        }
      case _ => // do nothing
    }
  }

  private def writeNode (name: String, label: String)(implicit dot: StringBuilder) {
    dot append name
    dot append "["
    dot append "label="
    dot append quote (label)
    dot append "]"
    dot append (newline)
  }

  private def writeHeader (name: String)(implicit dot: StringBuilder) {
    dot append ("graph ")
    dot append (name)
    dot append (newline)
  }

  private def writeOpen (implicit dot: StringBuilder) {
    dot append ("{")
    dot append (newline)
  }

  private def writeClose (implicit dot: StringBuilder) {
    dot append ("}")
  }

  private def sub (s: String): String = "<SUB>" + s + "</SUB>"

  private def quote (s: String): String = {
    "\"" + s.replaceAll ("\"", "&quot;") + "\""
  }

  private val newline = "\\n"
}
