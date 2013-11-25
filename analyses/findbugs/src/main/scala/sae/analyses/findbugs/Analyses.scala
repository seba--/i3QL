package sae.analyses.findbugs

import sae.analyses.findbugs.random._
import sae.analyses.findbugs.selected._

/**
 * @author Mirko Köhler
 */
object Analyses {

  private val analysesList : List[FindbugsAnalysis[_]] =
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


  def apply (name : String) : FindbugsAnalysis[_] =
    analysesList.find( (analysis) => analysis.getClass.getName == name ) match {
      case Some (a) => a
    }

}
