package sae.analyses.findbugs

import sae.bytecode.BytecodeDatabase
import idb.Relation

/**
 * @author Mirko Köhler
 */
trait FindbugsAnalysis[T] extends (BytecodeDatabase => Relation[T]) {

}
