package sandbox.findbugs

import detect.Detector
import sae.{Relation, Observer}
import sae.deltas.{Deletion, Addition, Update}
import sandbox.stackAnalysis.instructionInfo.StateInfo
import sae.syntax.sql._
import collection.mutable
import de.tud.cs.st.bat.resolved.Instruction
import sae.bytecode.structure.{CodeInfo, MethodDeclaration}
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis._
import sae.operators.impl.TransactionalEquiJoinView
import sandbox.stackAnalysis.codeInfo.MethodResultToStateInfoView


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 15.02.13
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
class StateInfoToBugInfoView(source : Relation[StateInfo],
               detector : Detector)
  extends Relation[BugInfo] with Observer[StateInfo] {

  source.addObserver(this)

  private var additions : mutable.HashMap[(MethodDeclaration,Int), StateInfo] = mutable.HashMap.empty[(MethodDeclaration,Int), StateInfo]
  private var deletions : mutable.HashMap[(MethodDeclaration,Int), StateInfo] = mutable.HashMap.empty[(MethodDeclaration,Int), StateInfo]

  def isSet: Boolean = false
  def isStored : Boolean = false

  // update operations on right relation
  def updated(oldV: StateInfo, newV: StateInfo) {
    removed(oldV)
    added(newV)
  }

  override def removed(v: StateInfo) {
    val key = (v.declaringMethod, v.pc)
    deletions.get(key) match {
      case Some(stateInfo) =>
        //If a state info is stored at the key of the new store, generate the upper bound of the state.
        deletions.remove(key)
        deletions.put(key,StateInfo(stateInfo.declaringMethod,stateInfo.pc,stateInfo.instruction,stateInfo.state.upperBound(v.state)))
      case None =>
        //Else just store the state info
        deletions.put((v.declaringMethod,v.pc),v)
    }

  }

  override def added(v: StateInfo) {
    val key = (v.declaringMethod, v.pc)

    additions.get(key) match {
      case Some(stateInfo) => {
        //If a state info is stored at the key of the new store, generate the upper bound of the state.
        additions.remove(key)
        additions.put(key,StateInfo(stateInfo.declaringMethod,stateInfo.pc,stateInfo.instruction,stateInfo.state.upperBound(v.state)))
      }
      case None =>
        //Else just store the state info
        additions.put((v.declaringMethod,v.pc),v)
    }

  }

  private def computeAdditions() = {

    for(stateInfo <- additions.valuesIterator) {
      val checkFunction = detector.getDetectorFunction(stateInfo.instruction)
      for (stack <- stateInfo.state.stacks.stacks) {
        val check : Option[BugType.Value] = checkFunction( stateInfo.pc,  stateInfo.instruction, stack, stateInfo.state.variables)
        check match {
          case Some(bug) =>
            element_added(BugInfo(stateInfo.declaringMethod, stateInfo.pc, bug))
          case None =>
            None
        }
      }
    }
  }

  private def computeDeletions() = {
    for(stateInfo <- deletions.valuesIterator) {
      val checkFunction = detector.getDetectorFunction(stateInfo.instruction)
      for (stack <- stateInfo.state.stacks.stacks) {
        val check : Option[BugType.Value] = checkFunction( stateInfo.pc,  stateInfo.instruction, stack, stateInfo.state.variables)
        check match {
          case Some(bug) =>
            element_removed(BugInfo(stateInfo.declaringMethod, stateInfo.pc, bug))
          case None =>
            None
        }
      }
    }
  }

  def clear() {
    additions = mutable.HashMap.empty[(MethodDeclaration,Int),StateInfo]
    deletions = mutable.HashMap.empty[(MethodDeclaration,Int),StateInfo]
  }

  override def endTransaction() {
    computeAdditions()
    computeDeletions()
    clear()
    super.endTransaction()
  }

  def updated[U <: StateInfo](update: Update[U]) {
    throw new UnsupportedOperationException
  }

  def modified[U <: StateInfo](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
    throw new UnsupportedOperationException
  }

  def foreach[T](f: (BugInfo) => T) {
    /* do nothing, since this is a transactional view */
  }
}

case object StateInfoToBugInfoView {
  def apply(source : Relation[StateInfo], detector : Detector) : StateInfoToBugInfoView = {
    return new StateInfoToBugInfoView(source,detector)
  }

  def apply(sourceCodeInfo : Relation[CodeInfo], sourceMethodStates : Relation[MethodStates], detector : Detector) : StateInfoToBugInfoView = {
      return new StateInfoToBugInfoView(new MethodResultToStateInfoView(sourceCodeInfo,sourceMethodStates),detector)
  }

}
