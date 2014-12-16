package sae.analyses.profiler.util

import java.io.InputStream

/**
 * @author Mirko Köhler
 */
trait DatabaseReader {

  def addArchive (stream : java.io.InputStream)

  def addClassFile (stream : java.io.InputStream)
  def removeClassFile (stream : java.io.InputStream)
  def updateClassFile (oldStream : java.io.InputStream, newStream : java.io.InputStream)

  def addClassFiles (streams : Seq[InputStream])
  def removeClassFiles (streams : Seq[InputStream])
  def updateClassFiles (oldStreams : Seq[InputStream], newStreams : Seq[InputStream])

  def classCount : Int
  def methodCount : Int
  def fieldCount : Int
  def instructionCount : Int

}
