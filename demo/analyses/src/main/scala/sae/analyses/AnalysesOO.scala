/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.analyses

import findbugs.base.oo.Definitions
import sae.Relation
import sae.bytecode.BytecodeDatabase
import findbugs.random.oo._
import findbugs.selected.oo._
import metrics.{LCOMStar, DepthOfInheritanceTree}


/**
 *
 * @author Ralf Mitschke
 *
 */
object AnalysesOO
{



    def apply(analysisName: String, database: BytecodeDatabase)(existsOptimization: Boolean, transactional: Boolean, shared: Boolean): Relation[_] = {
        val optimized = existsOptimization || transactional
        if (!optimized) {
            Definitions.shared = shared
            Definitions.transactional = false
            Definitions.existsOptimization = false
            sae.ENABLE_FORCE_TO_SET = false
            getBase (analysisName, database)
        }
        else
        {
            Definitions.transactional = transactional
            Definitions.existsOptimization = existsOptimization
            Definitions.shared = shared
            sae.ENABLE_FORCE_TO_SET = true
            getOptimized (analysisName, database)
        }
    }

    private def getBase(analysisName: String, database: BytecodeDatabase): Relation[_] = analysisName match {
        case "CI_CONFUSED_INHERITANCE" => CI_CONFUSED_INHERITANCE (database)
        case "CN_IDIOM" => CN_IDIOM (database)
        case "CN_IDIOM_NO_SUPER_CALL" => CN_IDIOM_NO_SUPER_CALL (database)
        case "CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE" => CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE (database)
        case "CO_ABSTRACT_SELF" => CO_ABSTRACT_SELF (database)
        case "CO_SELF_NO_OBJECT" => CO_SELF_NO_OBJECT (database)
        case "DM_GC" => DM_GC (database)
        case "DM_RUN_FINALIZERS_ON_EXIT" => DM_RUN_FINALIZERS_ON_EXIT (database)
        case "EQ_ABSTRACT_SELF" => EQ_ABSTRACT_SELF (database)
        case "FI_PUBLIC_SHOULD_BE_PROTECTED" => FI_PUBLIC_SHOULD_BE_PROTECTED (database)
        case "IMSE_DONT_CATCH_IMSE" => IMSE_DONT_CATCH_IMSE (database)
        case "SE_NO_SUITABLE_CONSTRUCTOR" => SE_NO_SUITABLE_CONSTRUCTOR (database)
        case "UUF_UNUSED_FIELD" => UUF_UNUSED_FIELD (database)
        /* randomly selected analyses without dataflow */
        case "BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION" => BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION (database)
        case "DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT" => DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT (database)
        case "DP_DO_INSIDE_DO_PRIVILEGED" => DP_DO_INSIDE_DO_PRIVILEGED (database)
        case "FI_USELESS" => FI_USELESS (database)
        case "ITA_INEFFICIENT_TO_ARRAY" => ITA_INEFFICIENT_TO_ARRAY (database)
        case "MS_PKGPROTECT" => MS_PKGPROTECT (database)
        case "MS_SHOULD_BE_FINAL" => MS_SHOULD_BE_FINAL (database)
        case "SE_BAD_FIELD_INNER_CLASS" => SE_BAD_FIELD_INNER_CLASS (database)
        case "SIC_INNER_SHOULD_BE_STATIC_ANON" => SIC_INNER_SHOULD_BE_STATIC_ANON (database)
        case "SW_SWING_METHODS_INVOKED_IN_SWING_THREAD" => SW_SWING_METHODS_INVOKED_IN_SWING_THREAD (database)
        case "UG_SYNC_SET_UNSYNC_GET" => UG_SYNC_SET_UNSYNC_GET (database)
        case "UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR" => UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR (database)
        /* selected metrics */
        case "DIT" => DepthOfInheritanceTree (database)
        case "LCOM" => LCOMStar (database)
        case _ => throw new IllegalArgumentException ("Unknown analysis: " + analysisName)
    }

    private def getOptimized(analysisName: String, database: BytecodeDatabase): Relation[_] = analysisName match {
        case "CI_CONFUSED_INHERITANCE" => CI_CONFUSED_INHERITANCE (database)
        case "CN_IDIOM" => findbugs.selected.oo.optimized.CN_IDIOM (database)
        case "CN_IDIOM_NO_SUPER_CALL" => findbugs.selected.oo.optimized.CN_IDIOM_NO_SUPER_CALL (database)
        case "CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE" => findbugs.selected.oo.optimized.CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE (database)
        case "CO_ABSTRACT_SELF" => findbugs.selected.oo.optimized.CO_ABSTRACT_SELF (database)
        case "CO_SELF_NO_OBJECT" => findbugs.selected.oo.optimized.CO_SELF_NO_OBJECT (database)
        case "DM_GC" => DM_GC (database)
        case "DM_RUN_FINALIZERS_ON_EXIT" => DM_RUN_FINALIZERS_ON_EXIT (database)
        case "EQ_ABSTRACT_SELF" => EQ_ABSTRACT_SELF (database)
        case "FI_PUBLIC_SHOULD_BE_PROTECTED" => FI_PUBLIC_SHOULD_BE_PROTECTED (database)
        case "IMSE_DONT_CATCH_IMSE" => IMSE_DONT_CATCH_IMSE (database)
        case "SE_NO_SUITABLE_CONSTRUCTOR" => findbugs.selected.oo.optimized.SE_NO_SUITABLE_CONSTRUCTOR (database)
        case "UUF_UNUSED_FIELD" => findbugs.selected.oo.optimized.UUF_UNUSED_FIELD (database)
        /* randomly selected analyses without dataflow */
        case "BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION" => findbugs.random.oo.optimized.BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION (database)
        case "DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT" => findbugs.random.oo.optimized.DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT (database)
        case "DP_DO_INSIDE_DO_PRIVILEGED" => DP_DO_INSIDE_DO_PRIVILEGED (database)
        case "FI_USELESS" => findbugs.random.oo.optimized.FI_USELESS (database)
        case "ITA_INEFFICIENT_TO_ARRAY" => findbugs.random.oo.optimized.ITA_INEFFICIENT_TO_ARRAY (database)
        case "MS_PKGPROTECT" => findbugs.random.oo.optimized.MS_PKGPROTECT (database)
        case "MS_SHOULD_BE_FINAL" => MS_SHOULD_BE_FINAL (database)
        case "SE_BAD_FIELD_INNER_CLASS" => findbugs.random.oo.optimized.SE_BAD_FIELD_INNER_CLASS (database)
        case "SIC_INNER_SHOULD_BE_STATIC_ANON" => findbugs.random.oo.optimized.SIC_INNER_SHOULD_BE_STATIC_ANON (database)
        case "SW_SWING_METHODS_INVOKED_IN_SWING_THREAD" => SW_SWING_METHODS_INVOKED_IN_SWING_THREAD (database)
        case "UG_SYNC_SET_UNSYNC_GET" => findbugs.random.oo.optimized.UG_SYNC_SET_UNSYNC_GET (database)
        case "UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR" => findbugs.random.oo.optimized.UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR (database)
        /* selected metrics */
        case "DIT" => DepthOfInheritanceTree (database)
        case "LCOM" => LCOMStar (database)
        case _ => throw new IllegalArgumentException ("Unknown analysis: " + analysisName)
    }


}
