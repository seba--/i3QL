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
package sae.analyses.findbugs

import org.junit.{Ignore, Test}
import sae.analyses.findbugs.random._


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

class TestRandomAnalysesOnJDK extends AbstractTestAnalysesOnJDK
{


	@Test
	def test_BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION () {
		executeAnalysis (BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION, expectedMatches = 0)
		//Findbugs says 3
	}

	@Test
	def test_DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT () {
		executeAnalysis(DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT, expectedMatches = 0)
		//Findbugs says 0
	}

	@Test
    def test_DP_DO_INSIDE_DO_PRIVILEGED () {
		executeAnalysis(DP_DO_INSIDE_DO_PRIVILEGED, expectedMatches = 10)
		//Findbugs says 0
	}

	@Test
	def test_FI_USELESS () {
		executeAnalysis(FI_USELESS, expectedMatches = 2)
		//Findbugs says 0
	}

	@Test
	def test_ITA_INEFFICIENT_TO_ARRAY () {
        executeAnalysis(ITA_INEFFICIENT_TO_ARRAY, expectedMatches = 32)
		//Findbugs says 0
	}

	@Test
	def test_MS_PKGPROTECT () {
		executeAnalysis(MS_PKGPROTECT, expectedMatches = 0)
		//Findbugs says 61
	}

	@Test
	def test_MS_SHOULD_BE_FINAL () {
		executeAnalysis(MS_SHOULD_BE_FINAL, expectedMatches = 0)
		//Findbugs says 32
	}

	@Test
	def test_SE_BAD_FIELD_INNER_CLASS () {
		executeAnalysis(SE_BAD_FIELD_INNER_CLASS, expectedMatches = 0)
		//Findbugs says 3
	}

	@Test
	def test_SW_SWING_METHODS_INVOKED_IN_SWING_THREAD () {
		executeAnalysis(SW_SWING_METHODS_INVOKED_IN_SWING_THREAD, expectedMatches = 0)
		//Findbugs says 0
	}

	@Test
	def test_UG_SYNC_SET_UNSYNC_GET () {
		executeAnalysis(UG_SYNC_SET_UNSYNC_GET, expectedMatches = 32)
		//Findbugs says 35
	}

	@Test
	def test_UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR () {
		executeAnalysis(UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR, expectedMatches = 0)
		//Findbugs says 1
	}


}
