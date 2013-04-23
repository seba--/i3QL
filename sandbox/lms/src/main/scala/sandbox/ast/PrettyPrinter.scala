package sandbox.ast

import sandbox.ast.sql.operators._

/**
 *
 * @author Ralf Mitschke
 */
object PrettyPrinter
{

  def apply (operator: Operator, name: String = "iql"): String =
  {
    implicit val dot = new StringBuilder
    writePrelude (name)

    writeOpen

    writeHeader

    implicit val nodes =
      writeNodes (operator)

    writeEdges (operator)

    writeClose

    dot.toString ()
  }


  private case class Node (name: String)
  {
    def this (num: Int) =
    {
      this ("node_" + num)
    }
  }

  private def writeNodes (operator: Operator)
      (implicit dot: StringBuilder, nodeMap: Map[Operator, Node] = Map.empty): Map[Operator, Node] =
  {
    val node = nodeMap.getOrElse (operator, new Node (nodeMap.size))
    operator match {
      case Selection (_, function) => {
        writeNode (node.name, "σ")
      }
      case TableReference (table) =>
        writeNode (node.name, table.name)
    }

    implicit var nodes = nodeMap.updated (operator, node)
    for (child <- operator.children) {
      nodes = writeNodes (child)(dot, nodes)
    }
    nodes
  }

  private def writeEdges (operator: Operator)(implicit dot: StringBuilder, nodeMap: Map[Operator, Node])
  {
    for (child <- operator.children) {
      val startNode = nodeMap (operator)
      val endNode = nodeMap (child)
      dot append "  "
      dot append startNode.name
      dot append " -- "
      dot append endNode.name
      dot append newline
    }
  }

  private def writeNode (name: String, label: String)(implicit dot: StringBuilder)
  {
    dot append "  "
    dot append name
    dot append " "
    dot append "["
    dot append "label="
    dot append quote (label)
    dot append "]"
    dot append ";"
    dot append newline
  }

  private def writeHeader (implicit dot: StringBuilder)
  {
    dot append "  node [shape=none];"
    dot append newline
  }

  private def writePrelude (name: String)(implicit dot: StringBuilder)
  {
    dot append "graph "
    dot append name
    dot append newline
  }

  private def writeOpen (implicit dot: StringBuilder)
  {
    dot append "{"
    dot append newline
  }

  private def writeClose (implicit dot: StringBuilder)
  {
    dot append "}"
  }

  private def sub (s: String): String = "<SUB>" + s + "</SUB>"

  private def quote (s: String): String =
  {
    "\"" + s.replaceAll ("\"", "&quot;") + "\""
  }

  private val newline = "\n"
}
