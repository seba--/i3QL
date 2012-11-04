package sandbox.stackAnalysis

import sae.Relation
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.{CFGEntry, AnalysisCFG}


/**
 * This class implements a control flow graph based on a Relation[CodeInfo].
 * This graph is used for forward analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 24.10.12
 * Time: 14:33
 */
class CodeInfoCFG(codeInfo: Relation[CodeInfo]) extends AnalysisCFG {

  /*Overrides the trait function*/
  val predecessors: Relation[CFGEntry] = compile(SELECT((c: CodeInfo) => {
    print("<" + c.declaringMethod.name + ">")
   CFGEntry(c.declaringMethod, computePredecessorsPriv(c.code.instructions))
  }) FROM codeInfo)

  /**
   * This method computes the preceding program counters based on an Array[Instruction]
   * @param a The instructions of a method on which a single control flow graph should be created.
   * @return The array of predecessors of a method. The indexes of the array are the valid program
   *         counters of the program. The result on non-PC indexes is null.
   */
  private def computePredecessorsPriv(a: Array[Instruction]): Array[List[Int]] = {

    val res = Array.fill[List[Int]](a.length)(null)
    res(0) = Nil

    var currentPC = 0
    var nextPC = 0

    while (nextPC != -1) {

      nextPC = CodeInfoTools.getNextPC(a, currentPC)

      if (nextPC != -1) {
        if (a(currentPC).isInstanceOf[ConditionalBranchInstruction]) {
          addToArray(res, nextPC, currentPC)
          addToArray(res, currentPC + a(currentPC).asInstanceOf[ConditionalBranchInstruction].branchoffset, currentPC)
        } else if (a(currentPC).isInstanceOf[UnconditionalBranchInstruction]) {
          addToArray(res, currentPC + a(currentPC).asInstanceOf[UnconditionalBranchInstruction].branchoffset, currentPC)
        } else {
          addToArray(res, nextPC, currentPC)
        }
      }

      currentPC = nextPC
    }

    println(res.mkString("CFGRes: ", ", ", ""))
    return res
  }

  /**
   * This method add an element to a list in an array at a specified index.
   * @param a The array where the lists are stored.
   * @param index The index in the array.
   * @param add The element that should be added to a list.
   */
  private def addToArray(a: Array[List[Int]], index: Int, add: Int) {
    if (a(index) == null)
      a(index) = Nil
    a(index) = add :: a(index)
  }


}
