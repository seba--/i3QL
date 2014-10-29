package sae.analyses.findbugs

import sae.analyses.findbugs.random._
import sae.analyses.findbugs.selected._
import sae.bytecode.BytecodeDatabase
import idb.Relation
import scala.collection._

/**
 * @author Mirko Köhler
 */
class Analyses(database : BytecodeDatabase) {

	private val analysesList : List[(BytecodeDatabase => Relation[_])] =
		BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION ::
		CI_CONFUSED_INHERITANCE ::
		CN_IDIOM ::
		CN_IDIOM_NO_SUPER_CALL ::
		CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE ::
		CO_ABSTRACT_SELF ::
		CO_SELF_NO_OBJECT ::
		DM_GC ::
		DM_RUN_FINALIZERS_ON_EXIT ::
		DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT ::
		DP_DO_INSIDE_DO_PRIVILEGED ::
		EQ_ABSTRACT_SELF ::
		FI_PUBLIC_SHOULD_BE_PROTECTED ::
		FI_USELESS ::
		IMSE_DONT_CATCH_IMSE ::
		ITA_INEFFICIENT_TO_ARRAY ::
		MS_PKGPROTECT ::
		MS_SHOULD_BE_FINAL ::
		NONE ::
		SE_BAD_FIELD_INNER_CLASS ::
		SE_NO_SUITABLE_CONSTRUCTOR ::
		SS_SHOULD_BE_STATIC ::
		SW_SWING_METHODS_INVOKED_IN_SWING_THREAD ::
		UG_SYNC_SET_UNSYNC_GET ::
		UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR ::
		UUF_UNUSED_FIELD ::
		Nil

	private val compiledAnalyses : mutable.HashMap[String, Relation[_]] = mutable.HashMap.empty[String, Relation[_]]


	def apply (name : String) : Relation[_] = {
		compiledAnalyses.get(name) match {
			case Some (relation) => relation
			case None => {
				analysesList.find( (analysis) => analysisName(analysis) == name ) match {
					case Some (a) => {
						val result = a(database)
						compiledAnalyses.put(name, result)
						result
					}
					case None => throw new IllegalArgumentException ("Analysis with the name " + name + " is unknown.")
				}
			}
		}
	}

	def all : List[(String, Relation[_])] = {
		analysesList.map((f) => (f.getClass.getSimpleName, f(database)))

	}

	private def analysisName(analysis : BytecodeDatabase => _) : String = {
		val className = analysis.getClass.getSimpleName
		if (className.length > 0 && className.last == '$')
			className.substring(0, className.length - 1)
		else
			""
	}





}
