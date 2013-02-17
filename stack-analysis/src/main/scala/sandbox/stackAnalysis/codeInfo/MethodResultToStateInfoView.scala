package sandbox.stackAnalysis.codeInfo

import sae.{Observer, Relation}
import sandbox.stackAnalysis.instructionInfo.StateInfo
import sae.deltas.{Deletion, Addition, Update}
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis._
import sandbox.stackAnalysis.instructionInfo.StateInfo
import de.tud.cs.st.bat.resolved.Instruction
import sandbox.stackAnalysis.datastructure.State
import sae.operators.impl.TransactionalEquiJoinView

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.02.13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
class MethodResultToStateInfoView (sourceCodeInfo : Relation[CodeInfo], sourceMethodStates : Relation[MethodStates])
  extends Relation[StateInfo] with Observer[(MethodDeclaration, IndexedSeq[Instruction], IndexedSeq[State])] {

  type SourceElement = (MethodDeclaration, IndexedSeq[Instruction], IndexedSeq[State])

  val source : Relation[SourceElement] =
    new TransactionalEquiJoinView[CodeInfo,MethodStates,SourceElement,MethodDeclaration](
      sourceCodeInfo,sourceMethodStates,
      ci => ci.declaringMethod,
      ms => ms.declaringMethod,
      (ci : CodeInfo, ms : MethodStates) => (ci.declaringMethod, ci.code.instructions, ms.states)
    )

  source.addObserver(this)

  def isSet: Boolean = false
  def isStored : Boolean = false

  // update operations on right relation
  override def updated(oldV: SourceElement, newV: SourceElement) {
    removed(oldV)
    added(newV)
  }

  override def removed(v: SourceElement) {
    val method = v._1
    val instructions = v._2
    val states = v._3

    var pc : Int = 0

    while(pc < instructions.size) {
      if(instructions(pc) != null) {
        element_removed(StateInfo(method,pc,instructions(pc),states(pc)))
      }
      pc = pc + 1
    }
  }

  override def added(v: SourceElement) {
    val method = v._1
    val instructions = v._2
    val states = v._3

    var pc : Int = 0

    while(pc < instructions.size) {
      if(instructions(pc) != null) {
        element_added(StateInfo(method,pc,instructions(pc),states(pc)))
      }
      pc = pc + 1
    }
  }



  override def endTransaction() {
    notifyEndTransaction()
  }

  def updated[U <: SourceElement](update: Update[U]) {
    throw new UnsupportedOperationException
  }

  def modified[U <: SourceElement](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
    throw new UnsupportedOperationException
  }

  def foreach[T](f: (StateInfo) => T) {
    /* do nothing, since this is a transactional view */
  }
}

