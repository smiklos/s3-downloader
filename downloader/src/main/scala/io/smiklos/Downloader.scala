package io.smiklos

import java.io.{BufferedWriter, File, FileWriter}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, RequestEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.alpakka.kinesis.ShardSettings
import akka.stream.alpakka.kinesis.scaladsl.KinesisSource
import akka.stream.scaladsl.Source
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder
import com.amazonaws.services.kinesis.model.{DescribeStreamRequest, ShardIteratorType}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConverters._
import scala.concurrent.Future

object Downloader extends App {

  val REGION = "eu-west-1"

  val streamName = args(0)
  val location = args(1)

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val kinesisClient = Option(System.getenv("KINESIS_HOST")) match {
    case None => AmazonKinesisAsyncClientBuilder.standard().withRegion(REGION).build()
    case Some(endpoint) => AmazonKinesisAsyncClientBuilder.standard()
      .withEndpointConfiguration(new EndpointConfiguration(endpoint, REGION)).build()
  }

  val s3Client = Option(System.getenv("S3_HOST")) match {
    case None => AmazonS3ClientBuilder.standard().withRegion(REGION).withPathStyleAccessEnabled(true).build()
    case Some(endpoint) => AmazonS3ClientBuilder.standard().withPathStyleAccessEnabled(true)
      .withEndpointConfiguration(new EndpointConfiguration(endpoint, REGION)).build()
  }


  val settings = kinesisClient.describeStream(new DescribeStreamRequest().withStreamName(streamName))
    .getStreamDescription.getShards.asScala
    .map(shard => ShardSettings(streamName = streamName, shardId = shard.getShardId)
      .withShardIteratorType(ShardIteratorType.LATEST)).toList

  val source: Source[com.amazonaws.services.kinesis.model.Record, NotUsed] =
    KinesisSource.basicMerge(settings, kinesisClient)


  val idGenerator = new AtomicInteger()


  val http = Http(system)

  def getContentLength(bucket: String, fileName: String): Int = {
    s3Client.getObjectMetadata(bucket, fileName).getContentLength.toInt
  }

  def route = pathPrefix("download" / Remaining) { fileName =>
    println("executing")
    get {
          complete {
                     val id = idGenerator.getAndIncrement()
                     val now = System.nanoTime()
                     val contentLength = getContentLength("infare-dev-test", fileName)
                     val splits = Splitter.split(contentLength, id)
                     val request = SplitRequest("infare-dev-test", fileName, splits)
                     DownloadManager.register(id, DownloadRequest(fileName, splits.size, now))
                     post(request).map(_ => StatusCodes.OK)
                   }
        }
                                                 }

  source.statefulMapConcat(() => {
    record => {
      val Array(id, part) = record.getPartitionKey.split("-").map(_.toInt)
      DownloadManager.handleChunk(id, Chunk(part, record.getData.array())) match {
        case Partial => List()
        case Completed(request, parts) => {
          val finished = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - request.downloadStartedNano)
          println(s"Download took $finished ms")
          List((request.fileName, DownloadManager.assemble(parts)))
        }
      }
    }
  }

  ).runForeach(fileAndContent => writeFile(location, fileAndContent._1, fileAndContent._2))

  Http().bindAndHandle(route, "localhost", 8081)

  private def writeFile(location: String, fileName: String, bytes: Array[Byte]): Unit = {
    val file = new File(location + File.separator + fileName)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(new String(bytes, "UTF-8"))
    bw.close()
  }

  private def post(splitRequest: SplitRequest): Future[HttpResponse] = {
    println("posting")
    implicit val splitFormat = jsonFormat2(FileSplit)

    implicit val splitRequestFormat = jsonFormat3(SplitRequest)
    Marshal(splitRequest).to[RequestEntity] flatMap { entity =>
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://127.0.0.1:5454/split",
                                entity = entity)
      http.singleRequest(request)
    }
  }
}

case class Chunk(part: Int, data: Array[Byte])

case class SplitRequest(bucket: String, key: String, splits: List[FileSplit])
