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
import org.junit.Assert._
import sae.bytecode.ASMDatabaseFactory
import sae.analyses.findbugs.selected._
import idb.algebra.print.RelationalAlgebraPrintPlan

/**
 *
 * @author Ralf Mitschke
 *
 */

class TestAnalysesOnJDK
{

    def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")

    def getDatabase = ASMDatabaseFactory.create ()

    @Ignore
    @Test
    def test_CI_CONFUSED_INHERITANCE () {
        val database = getDatabase
        val analysis = CI_CONFUSED_INHERITANCE (database).asMaterialized
        database.addArchive (getStream)
        assertEquals (123, analysis.size)
    }

    @Ignore // TODO not correct yet
    @Test
    def test_CN_IDIOM() {
        val database = getDatabase
        val analysis = CN_IDIOM (database).asMaterialized
        database.addArchive (getStream)
        assertEquals (835, analysis.size)
    }

    @Ignore // TODO not correct yet
    @Test
    def test_CN_IDIOM_NO_SUPER_CALL () {
        val database = getDatabase
        val analysis = CN_IDIOM_NO_SUPER_CALL (database).asMaterialized
        database.addArchive (getStream)
        assertEquals (136, analysis.size)
    }

    //@Ignore // TODO not correct yet
    @Test
    def test_CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE() {
        val database = getDatabase
        val analysis = CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE (database).asMaterialized
        database.addArchive (getStream)
        //assertEquals (38, analysis.size)
        analysis.foreach(println)
    }


    @Ignore
    @Test
    def test_EQ_ABSTRACT_SELF () {
        val database = getDatabase
        val analysis = EQ_ABSTRACT_SELF (database).asMaterialized
        database.addArchive (getStream)
        assertEquals (4, analysis.size)
    }

    @Ignore
    @Test
    def test_FI_PUBLIC_SHOULD_BE_PROTECTED () {
        val database = getDatabase
        val analysis = FI_PUBLIC_SHOULD_BE_PROTECTED (database).asMaterialized
        database.addArchive (getStream)
        assertEquals (20, analysis.size)
    }
}
