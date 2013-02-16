package sandbox.stackAnalysis.codeInfo

import sae.Relation
import sae.bytecode.BytecodeDatabase
import sae.syntax.sql._
import sae.bytecode.structure.CodeInfo

/**
 * This trait is used to define control flow graphs for the data flow analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
trait ControlFlowAnalysis extends (BytecodeDatabase => Relation[MethodCFG]) {
  /**
   * This function uses the SQL Queries to create a new relation of type (MethodDeclaration, Array[List[Int]]).
   * SQL Queries should be used to guarantee incrementalization.
   * @return A relation of type(MethodDeclaration, Array[List[Int]]). The first parameter refers to the method
   *         which is underlying to the control flow graph. The second parameter defines the control flow graph,
   *         by defining the list of preceding program counters. The indexes of the array are the program counters
   *         for instructions in the code.
   */
  //def newResult: Relation[MethodCFG]

  def apply(bcd: BytecodeDatabase): Relation[MethodCFG] = {
    compile(SELECT((c: CodeInfo) => MethodCFG(c.declaringMethod, computePredecessors(c))) FROM bcd.code)
  }

  def computePredecessors(ci: CodeInfo): Array[List[Int]]
}
