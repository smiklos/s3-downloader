name := "cloud-fun"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-kinesis" % "1.0-M3"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.3"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.529"

val root = project.in(file("."))
  .settings(name := "cloud-fun")
  .aggregate(downloader)

lazy val downloader = project.in(file("downloader"))
  .settings(Settings.withCommonSettings: _*)
  .settings(Settings.withAssembly: _*)
  .settings(Settings.withTesting: _*)
  .settings(inThisBuild(libraryDependencies ++= Settings.configs))

