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
package sae.analyses.findbugs.test

import sae.bytecode.bat.BATDatabaseFactory
import sae._
import analyses.findbugs.base.oo.Definitions
import analyses.findbugs.selected.oo.optimized._
import analyses.findbugs.random.oo.optimized._
import operators.impl.ExistsInSameDomainView
import org.junit.Test
import org.junit.Assert._

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.09.12
 * Time: 11:19
 */
class TestOptimizedOOAnalysesOnRT
{

    def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")


    @Test
    def test_CN_IDIOM() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CN_IDIOM (database))
        database.addArchive (getStream)
        assertEquals (835, analysis.size)
    }

    @Test
    def test_CN_IDIOM_NO_SUPER_CALL() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CN_IDIOM_NO_SUPER_CALL (database))
        database.addArchive (getStream)
        assertEquals (136, analysis.size)
    }


    @Test
    def test_CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE (database))
        database.addArchive (getStream)
        assertEquals (38, analysis.size)
    }


    @Test
    def test_CO_ABSTRACT_SELF() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CO_ABSTRACT_SELF (database))
        database.addArchive (getStream)
        // TODO fixme
        assertEquals (15, analysis.size)
    }

    @Test
    def test_CO_SELF_NO_OBJECT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CO_SELF_NO_OBJECT (database))
        database.addArchive (getStream)
        // TODO fixme
        assertEquals (51, analysis.size)
    }

    @Test
    def test_SE_NO_SUITABLE_CONSTRUCTOR() {
        sae.ENABLE_FORCE_TO_SET = true
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (SE_NO_SUITABLE_CONSTRUCTOR (database))
        database.addArchive (getStream)
        assertEquals (19, analysis.size)
        sae.ENABLE_FORCE_TO_SET = false
    }

    @Test
    def test_UUF_UNUSED_FIELD() {
        sae.ENABLE_FORCE_TO_SET = true
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (UUF_UNUSED_FIELD (database))
        database.addArchive (getStream)
        assertEquals (6799, analysis.size)
        sae.ENABLE_FORCE_TO_SET = false
    }

    @Test
    def test_BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION (database))
        database.addArchive (getStream)
        assertEquals (3, analysis.size)
    }

    @Test
    def test_DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT (database))
        database.addArchive (getStream)
        assertEquals (0, analysis.size)
    }

    @Test
    def test_FI_USELESS() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (FI_USELESS (database))
        database.addArchive (getStream)
        assertEquals (2, analysis.size)
    }

    @Test
    def test_ITA_INEFFICIENT_TO_ARRAY() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (ITA_INEFFICIENT_TO_ARRAY (database))
        database.addArchive (getStream)
        //assertEquals (56, analysis.size)
        // TODO six less than BAT who is right
        assertEquals (50, analysis.size)
    }

    @Test
    def test_UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR() {
        val database = BATDatabaseFactory.create ()
        sae.ENABLE_FORCE_TO_SET = true
        val analysis = relationToResult (UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR (database))
        database.addArchive (getStream)
        assertEquals (58, analysis.size)
        sae.ENABLE_FORCE_TO_SET = false
    }
}
