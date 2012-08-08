package sae.bytecode;

/**
 * Author: Ralf Mitschke
 * Date: 07.08.12
 * Time: 12:11
 */
trait BytecodeDatabaseManipulation {

    def getAddClassFileFunction: (java.util.File) => Unit

    def getRemoveClassFileFunction: (java.util.File) => Unit

    def addClassFile(stream: java.io.InputStream)

    def removeClassFile(stream: java.io.InputStream)

    def addArchive(stream: java.io.InputStream)

    def removeArchive(stream: java.io.InputStream)
}
