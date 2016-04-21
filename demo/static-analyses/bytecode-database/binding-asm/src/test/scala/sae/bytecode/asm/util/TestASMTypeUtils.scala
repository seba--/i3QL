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
package sae.bytecode.asm.util

import org.objectweb.asm.Type
import org.junit.Test
import org.junit.Assert._

/**
 *
 * @author Ralf Mitschke
 */
class TestASMTypeUtils
    extends ASMTypeUtils
{

    @Test
    def testCreateObjectTypeFromString () {
        val t1 = Type.getType (classOf[java.lang.Object])
        val t2 = ObjectType ("java/lang/Object")
        assertEquals (t1, t2)
    }

    @Test
    def testGetObjectTypeName () {
        val t = Type.getType (classOf[java.lang.Object])
        val s1 = "java/lang/Object"
        val s2 = ObjectType_Name (t)
        assertEquals (s1, s2)
    }

    @Test
    def testGetObjectTypeClassName () {
        val t = Type.getType (classOf[java.lang.Object])
        val s = ObjectType_ClassName (t)
        assertEquals ("Object", s)
    }


    @Test
    def testGetObjectTypePackageName () {
        val t = Type.getType (classOf[java.lang.Object])
        val s = ObjectType_PackageName (t)
        assertEquals ("java/lang", s)
    }

    @Test
    def testGetObjectTypeDefaultPackageName () {
        val t = Type.getObjectType ("LTest;")
        val s = ObjectType_PackageName (t)
        assertEquals ("", s)
    }
}
