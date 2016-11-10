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

	private var totalBytes : Long = 0

	override val identifier: Int =
		144429

	override def includeManifest: Boolean =
		serializer.includeManifest

//	val t = new Thread(new Runnable {
//		override def run(): Unit = {
//			while (true) {
//				Thread.sleep(1000)
//				println(s"[TestSerializer][$this] $count events received: $totalBytes bytes")
//			}
//		}
//	})
//	t.start()


	var count = 0
	override def fromBinaryJava(bytes: Array[Byte], clazz: Class[_]): AnyRef = {
		count = count + 1
		totalBytes += bytes.length
		serializer.fromBinary(bytes, clazz)
	}

	override def toBinary(o: AnyRef): Array[Byte] =
		serializer.toBinary(o)


}
