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
package sae.bytecode.bat

import sae.bytecode.{BytecodeStructureRelations, BytecodeDatabaseManipulation}
import java.io.InputStream
import de.tud.cs.st.bat.reader.ClassFileReader
import java.util.zip.{ZipEntry, ZipInputStream}
import de.tud.cs.st.bat.resolved.{ArrayType, ObjectType}
import sae.bytecode.bat.reader.ZipStreamEntryWrapper

/**
 *
 * @author Ralf Mitschke
 */
trait BATDatabaseManipulation
    extends BytecodeDatabaseManipulation
    with BytecodeStructureRelations
{
    protected def additionReader: ClassFileReader

    protected def removalReader: ClassFileReader

    private def endTransaction () {
        classDeclarations.endTransaction ()
        methodDeclarations.endTransaction ()
        fieldDeclarations.endTransaction ()
        codeAttributes.endTransaction ()
        innerClassAttributes.endTransaction ()
        enclosingMethodAttributes.endTransaction ()
    }

    private def doAddClassFile (stream: InputStream) {
        additionReader.ClassFile (() => stream)
    }

    private def doRemoveClassFile (stream: InputStream) {
        removalReader.ClassFile (() => stream)
    }


    private def processArchive (stream: InputStream, processorFunction: InputStream => Unit) {
        val zipStream: ZipInputStream = new ZipInputStream (stream)
        var zipEntry: ZipEntry = null
        while ( {
            zipEntry = zipStream.getNextEntry
            zipEntry
        } != null)
        {
            if (!zipEntry.isDirectory && zipEntry.getName.endsWith (".class")) {
                processorFunction (new ZipStreamEntryWrapper (zipStream, zipEntry))
            }
        }
        ObjectType.cache.clear ()
        ArrayType.cache.clear ()
    }

    def addArchive (stream: InputStream) {
        processArchive (stream, addClassFile)
    }

    def addClassFile (stream: InputStream) {
        doAddClassFile (stream)
        endTransaction ()
    }

    def removeArchive (stream: InputStream) {
        processArchive (stream, removeClassFile)
    }

    def removeClassFile (stream: InputStream) {
        doRemoveClassFile (stream)
        endTransaction ()
    }

    def updateClassFile (oldStream: InputStream, newStream: InputStream) {
        doAddClassFile (newStream)
        doRemoveClassFile (newStream)
        endTransaction ()
    }
}
