package idb.remote

import java.io.ByteArrayInputStream

import akka.actor.ExtendedActorSystem
import akka.serialization.{JSerializer, JavaSerializer}
import akka.util.ClassLoaderObjectInputStream

/**
  * Created by mirko on 28.10.16.
  */
class TestSerializer(system : ExtendedActorSystem) extends JSerializer {

	private val serializer = new JavaSerializer(system)

	private var totalBytes = 0

	override val identifier: Int =
		144429

	override def includeManifest: Boolean =
		serializer.includeManifest

	override def fromBinaryJava(bytes: Array[Byte], clazz: Class[_]): AnyRef = {
		totalBytes += bytes.length
		println(s"[TestSerializer][$this] Received $totalBytes bytes")
		serializer.fromBinary(bytes, clazz)
	}

	override def toBinary(o: AnyRef): Array[Byte] =
		serializer.toBinary(o)


}
