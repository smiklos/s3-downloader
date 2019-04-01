package io.smiklos

import scala.annotation.tailrec

object Splitter {

  val MAX_RECORD_SIZE_BYTES = 1024 * 1024

  @tailrec
  def split(contentLength: Int, id: Int, part: Int = 0, chunks: List[FileSplit] = List()): List[FileSplit] = {
    if (contentLength == 0) return chunks

    val key = s"$id-$part"
    val keyLength = key.getBytes.length
    val chunkLength = Math.min(contentLength, MAX_RECORD_SIZE_BYTES - keyLength)
    val chunk = FileSplit(key, chunkLength)
    split(contentLength - chunkLength, id, part + 1, chunks :+ chunk)
  }
}

case class FileSplit(key: String, chunk: Int)