package sandbox.dataflowAnalysis

import sae.bytecode.structure.MethodDeclaration
import sae.Relation

/**
 * Defines the transformer functions of a data flow analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
trait ResultTransformer[T] {
  /**
   * The type of functions that are used in the analysis.
   */
  type Transformer = (T => T)

  /**
   * This function uses the SQL Queries to create a new relation of type (MethodDeclaration, Array[Transformer]).
   * SQL Queries should be used to guarantee incrementalization.
   * @return A relation of type(MethodDeclaration, Array[Transformer]). The first parameter refers to the method
   *         which is underlying to the transformer functions array. The second parameter defines the transformer
   *         functions that are used in the data flow analysis. The indexes of the array are the program counters
   *         for instructions in the code.
   */
  def functions : Relation[ TransformerEntry[T] ]
}
