package sae.analyses.findbugs

import sae.analyses.findbugs.random._
import sae.analyses.findbugs.selected._
import sae.bytecode.BytecodeDatabase
import idb.Relation

/**
 * @author Mirko Köhler
 */
object Analyses {

	private val analysesList : List[(BytecodeDatabase => Relation[_])] =
		BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION ::
		CI_CONFUSED_INHERITANCE ::
		CN_IDIOM ::
		CN_IDIOM_NO_SUPER_CALL ::
		CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE ::
		CO_ABSTRACT_SELF ::
		DM_GC ::
		DM_RUN_FINALIZERS_ON_EXIT ::
		DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT ::
		EQ_ABSTRACT_SELF ::
		FI_PUBLIC_SHOULD_BE_PROTECTED ::
		IMSE_DONT_CATCH_IMSE ::
		SE_NO_SUITABLE_CONSTRUCTOR ::
		SS_SHOULD_BE_STATIC ::
		UUF_UNUSED_FIELD ::
		Nil


	def apply (name : String) : (BytecodeDatabase => Relation[_]) = {
		analysesList.find( (analysis) => analysisName(analysis) == name ) match {
			case Some (a) => a
			case None => throw new IllegalArgumentException ("Analysis with the name " + name + " is unknown.")
		}
	}

	private def analysisName(analysis : BytecodeDatabase => _) : String = {
		val className = analysis.getClass.getSimpleName
		if (className.length > 0 && className.last == '$')
			className.substring(0, className.length - 1)
		else
			""
	}





}
