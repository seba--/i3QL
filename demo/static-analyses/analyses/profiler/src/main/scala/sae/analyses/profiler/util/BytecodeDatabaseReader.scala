package sae.analyses.profiler.util

import java.io.InputStream

import sae.bytecode.BytecodeDatabase

/**
 * @author Mirko Köhler
 */
class BytecodeDatabaseReader(val database: BytecodeDatabase) extends DatabaseReader {

	def addArchive(stream: java.io.InputStream) =
		database.addArchive(stream)

	def addClassFile(stream: java.io.InputStream) =
		database.addClassFile(stream)

	def removeClassFile(stream: java.io.InputStream) =
		database.removeClassFile(stream)

	def updateClassFile(oldStream: java.io.InputStream, newStream: java.io.InputStream) =
		database.updateClassFile(oldStream, newStream)

	val classes = database.classDeclarations.asMaterialized
	val methods = database.methodDeclarations.asMaterialized
	val fields = database.fieldDeclarations.asMaterialized
	val instructions = database.instructions.asMaterialized

	def classCount: Int = classes.size
	def methodCount: Int = methods.size
	def fieldCount: Int = fields.size
	def instructionCount: Int = instructions.size

	override def addClassFiles(streams: Seq[InputStream]) =
		database.addClassFiles(streams)

	override def removeClassFiles(streams: Seq[InputStream]) =
		database.removeClassFiles(streams)

	override def updateClassFiles(oldStreams: Seq[InputStream], newStreams: Seq[InputStream]) =
		database.updateClassFiles(oldStreams, newStreams)
}
