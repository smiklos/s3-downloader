package io.smiklos

import java.util.concurrent.ConcurrentHashMap

import scala.collection.mutable

object DownloadManager {

  val inFlightDownloads = new ConcurrentHashMap[Int, DownloadRequest]()
  val chunkBuffer = mutable.Map[Int, Array[Chunk]]()

  def handleChunk(id: Int, chunk: Chunk): DownloadState = {
    val extended = chunkBuffer.getOrElse(id, Array[Chunk]()) :+ chunk
    if (inFlightDownloads.get(id).parts == extended.length) {
      val request = inFlightDownloads.remove(id)
      chunkBuffer.remove(id)
      Completed(request, extended)
    }
    else {
      chunkBuffer.put(id, extended)
      println(s"Gathered ${extended.size} chunks for $id")
      Partial
    }
  }

  def register(id: Int, downloadRequest: DownloadRequest) = inFlightDownloads.put(id, downloadRequest)

  def assemble(parts: Array[Chunk]): Array[Byte] = parts.sortBy(_.part).map(_.data).fold(Array[Byte]())((
                                                                                                          left,
                                                                                                          right) => left ++ right)

}

case class DownloadRequest(fileName: String, parts: Int, downloadStartedNano: Long)

sealed trait DownloadState

case object Partial extends DownloadState

case class Completed(request: DownloadRequest, parts: Array[Chunk]) extends DownloadState