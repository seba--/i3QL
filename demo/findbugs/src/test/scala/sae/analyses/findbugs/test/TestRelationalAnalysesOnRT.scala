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
import analyses.findbugs.selected.relational._
import analyses.findbugs.random.relational._
import org.junit.Test
import org.junit.Assert._

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.09.12
 * Time: 11:19
 */

class TestRelationalAnalysesOnRT
{

    def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")


    @Test
    def test_CI_CONFUSED_INHERITANCE() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CI_CONFUSED_INHERITANCE (database))
        database.addArchive (getStream)
        assertEquals (123, analysis.size)
    }

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
    def test_CO_ABSTRACT_SELF() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CO_ABSTRACT_SELF (database))
        database.addArchive (getStream)
        assertEquals (16, analysis.size)
    }

    @Test
    def test_CO_SELF_NO_OBJECT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CO_SELF_NO_OBJECT (database))
        database.addArchive (getStream)
        assertEquals (55, analysis.size)
    }

    @Test
    def test_DM_GC() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (DM_GC (database))
        database.addArchive (getStream)
        assertEquals (3, analysis.size)
    }

    @Test
    def test_DM_RUN_FINALIZERS_ON_EXIT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (DM_RUN_FINALIZERS_ON_EXIT (database))
        database.addArchive (getStream)
        assertEquals (1, analysis.size)
    }

    @Test
    def test_EQ_ABSTRACT_SELF() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (EQ_ABSTRACT_SELF (database))
        database.addArchive (getStream)
        assertEquals (4, analysis.size)
    }

    @Test
    def test_FI_PUBLIC_SHOULD_BE_PROTECTED() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (FI_PUBLIC_SHOULD_BE_PROTECTED (database))
        database.addArchive (getStream)
        assertEquals (20, analysis.size)
    }

    @Test
    def test_IMSE_DONT_CATCH_IMSE() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (IMSE_DONT_CATCH_IMSE (database))
        database.addArchive (getStream)
        assertEquals (0, analysis.size)
    }

    @Test
    def test_SE_NO_SUITABLE_CONSTRUCTOR() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (SE_NO_SUITABLE_CONSTRUCTOR (database))
        database.addArchive (getStream)
        assertEquals (19, analysis.size)
    }

    @Test
    def test_UUF_UNUSED_FIELD() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (UUF_UNUSED_FIELD (database))
        database.addArchive (getStream)
        assertEquals (6799, analysis.size)
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
    def test_DP_DO_INSIDE_DO_PRIVILEGED() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (DP_DO_INSIDE_DO_PRIVILEGED (database))
        database.addArchive (getStream)
        assertEquals (27, analysis.size)
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
    def test_MS_PKGPROTECT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (MS_PKGPROTECT (database))
        database.addArchive (getStream)
        assertEquals (94, analysis.size)
    }

    @Test
    def test_MS_SHOULD_BE_FINAL() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (MS_SHOULD_BE_FINAL (database))
        database.addArchive (getStream)
        assertEquals (169, analysis.size)
    }

    @Test
    def test_SIC_INNER_SHOULD_BE_STATIC_ANON() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (SIC_INNER_SHOULD_BE_STATIC_ANON (database))
        database.addArchive (getStream)
        //assertEquals (572, analysis.size)
        //TODO five more than BAT who is right
        assertEquals (577, analysis.size)
    }

    @Test
    def test_SW_SWING_METHODS_INVOKED_IN_SWING_THREAD() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (SW_SWING_METHODS_INVOKED_IN_SWING_THREAD (database))
        database.addArchive (getStream)
        assertEquals (0, analysis.size)
    }

    @Test
    def test_UG_SYNC_SET_UNSYNC_GET() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (UG_SYNC_SET_UNSYNC_GET (database))
        database.addArchive (getStream)
        //assertEquals (31, analysis.size)
        //TODO 1 more than BAT who is right
        assertEquals (32, analysis.size)
    }

    @Test
    def test_UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR (database))
        database.addArchive (getStream)
        assertEquals (58, analysis.size)
    }
}
