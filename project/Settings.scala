import sbt.Keys.{libraryDependencies, _}
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object Settings {


  private val sparkVersion = "2.4.0"
  val withCommonSettings: Seq[Setting[_]] = {
    inThisBuild(Seq(
      organization := "com.infare.scheduler",
      scalaVersion := "2.11.12",
      parallelExecution := false,
      javaOptions ++= Seq(
        "-Xms512M",
        "-Xmx2048M",
        "-XX:MaxPermSize=2048M",
        "-XX:+CMSClassUnloadingEnabled"
      ),
      scalacOptions := Seq(
        "-feature",
        "-deprecation",
        "-Ypatmat-exhaust-depth", "off",
        "-Xmax-classfile-name", "144"
      )
    ))
  }

  val withTesting: Seq[Setting[_]] = Seq(
    parallelExecution in Test := false,
    fork in Test := true,
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
  )
  val configs: Seq[ModuleID] = Seq(
    "com.github.scopt" %% "scopt" % "3.7.0"
  )
  val withAssembly: Seq[Setting[_]] = baseAssemblySettings ++ Seq(
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "xml", xs@_*) => MergeStrategy.last
      case path if path.contains("commons-beanutils") || path.contains("commons-collections") => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }

  )

  val deps: Seq[ModuleID] = Seq(
    "com.lightbend.akka" %% "akka-stream-alpakka-kinesis" % "1.0-M3",
    "com.typesafe.akka" %% "akka-http" % "10.1.8",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
    "io.spray" %% "spray-json" % "1.3.3",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.11.529"
  )

}
