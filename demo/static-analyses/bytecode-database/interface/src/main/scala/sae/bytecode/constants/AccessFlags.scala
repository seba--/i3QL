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
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
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
package sae.bytecode.constants

/**
 *
 * @author Ralf Mitschke
 *
 */
object AccessFlags
{
    def contains (access_flags: Int, mask: Int): Boolean = (access_flags & mask) != 0

    // class, field, method
    val ACC_PUBLIC: Int = 0x0001
    // class, field, method
    val ACC_PRIVATE: Int = 0x0002
    // class, field, method
    val ACC_PROTECTED: Int = 0x0004
    // field, method
    val ACC_STATIC: Int = 0x0008
    // class, field, method
    val ACC_FINAL: Int = 0x0010
    // class
    val ACC_SUPER: Int = 0x0020
    // method
    val ACC_SYNCHRONIZED: Int = 0x0020
    // field
    val ACC_VOLATILE: Int = 0x0040
    // method
    val ACC_BRIDGE: Int = 0x0040
    // method
    val ACC_VARARGS: Int = 0x0080
    // field
    val ACC_TRANSIENT: Int = 0x0080
    // method
    val ACC_NATIVE: Int = 0x0100
    // class
    val ACC_INTERFACE: Int = 0x0200
    // class, method
    val ACC_ABSTRACT: Int = 0x0400
    // method
    val ACC_STRICT: Int = 0x0800
    // class, field, method
    val ACC_SYNTHETIC: Int = 0x1000
    // class
    val ACC_ANNOTATION: Int = 0x2000
    // class(?) field inner
    val ACC_ENUM: Int = 0x4000

    // class
    val ACC_CLASS_EXT: Int = ACC_INTERFACE | ACC_ANNOTATION | ACC_ENUM
    // class
    val ACC_ANNOTATION_EXT: Int = ACC_ANNOTATION | ACC_INTERFACE

}
