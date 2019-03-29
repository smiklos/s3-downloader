name := "cloud-fun"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-kinesis" % "1.0-M3"




val root = project.in(file("."))
  .settings(name := "cloud-fun")
  .aggregate(downloaderAgent, downloaderCli, downloadIniter)

lazy val downloaderAgent = project
  .settings(Settings.withCommonSettings: _*)
  .settings(Settings.withTesting: _*)
  .settings(inThisBuild(libraryDependencies ++= Settings.spark ++ Settings.configs ++ Settings.testing))

lazy val downloaderCli = project
  .settings(mainClass in assembly := Some("io.smiklos.Downloader"))
  .settings(Settings.withCommonSettings: _*)
  .settings(Settings.withTesting: _*)
  .settings(inThisBuild(libraryDependencies ++= Settings.databases ++ Settings.logging ++ Settings.infare ++ Settings.notebook))
