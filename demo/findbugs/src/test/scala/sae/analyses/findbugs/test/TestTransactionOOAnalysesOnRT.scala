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
import analyses.findbugs.selected.oo._
import org.junit.{Ignore, Test}
import org.junit.Assert._

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.09.12
 * Time: 11:19
 */

class TestTransactionOOAnalysesOnRT
{

    def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")

    @Test
    def test_CI_CONFUSED_INHERITANCE() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CI_CONFUSED_INHERITANCE (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (123, analysis.size)
    }

    @Test
    @Ignore
    def test_CN_IDIOM() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CN_IDIOM (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (835, analysis.size)
    }

    @Test
    @Ignore
    def test_CN_IDIOM_NO_SUPER_CALL() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CN_IDIOM_NO_SUPER_CALL (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (136, analysis.size)
    }

    @Test
    @Ignore
    def test_CO_ABSTRACT_SELF() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CO_ABSTRACT_SELF (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (16, analysis.size)
    }

    @Test
    @Ignore
    def test_CO_SELF_NO_OBJECT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (CO_SELF_NO_OBJECT (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (55, analysis.size)
    }

    @Test
    @Ignore
    def test_DM_GC() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (DM_GC (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (3, analysis.size)
    }

    @Test
    @Ignore
    def test_DM_RUN_FINALIZERS_ON_EXIT() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (DM_RUN_FINALIZERS_ON_EXIT (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (1, analysis.size)
    }

    @Test
    @Ignore
    def test_EQ_ABSTRACT_SELF() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (EQ_ABSTRACT_SELF (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (4, analysis.size)
    }

    @Test
    def test_FI_PUBLIC_SHOULD_BE_PROTECTED() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (FI_PUBLIC_SHOULD_BE_PROTECTED (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (20, analysis.size)
    }

    @Test
    @Ignore
    def test_IMSE_DONT_CATCH_IMSE() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (IMSE_DONT_CATCH_IMSE (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (0, analysis.size)
    }

    @Test
    @Ignore
    def test_SE_NO_SUITABLE_CONSTRUCTOR() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (SE_NO_SUITABLE_CONSTRUCTOR (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (19, analysis.size)
    }

    @Test
    @Ignore
    def test_UUF_UNUSED_FIELD() {
        val database = BATDatabaseFactory.create ()
        val analysis = relationToResult (UUF_UNUSED_FIELD (database))
        database.beginTransaction ()
        database.addArchive (getStream)
        database.commitTransaction ()
        assertEquals (6799, analysis.size)
    }


}
