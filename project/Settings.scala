import sbt.{Def, _}
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport.{ShadeRule, _}
import sbtbuildinfo.BuildInfoPlugin.autoImport._

object Settings {

  private val releasesRepository = "Sonatype Nexus Repository Manager" at "http://nexus.infare.net:8000/repository/maven-releases"
  private val snapshotsRepository = "Sonatype Snapshots Nexus Repository Manager" at "http://nexus.infare.net:8000/repository/maven-snapshots"

  private val sparkVersion = "2.4.0"
  val withCommonSettings: Seq[Setting[_]] = {
    inThisBuild(Seq(
      organization := "com.infare.scheduler",
      scalaVersion := "2.11.12",
      resolvers ++= Seq(snapshotsRepository, releasesRepository),
      credentials ++= Seq(
        Credentials(Path.userHome / ".ivy2" / ".credentials"),
        Credentials(Path.userHome / ".ivy2" / ".credentials.snapshots")
      ),
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

  val withBuildInfo: Seq[Setting[_]] = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, "sparkVersion" -> sparkVersion),
    buildInfoPackage := organization.value + "." + name.value.filter(_.isLetter),
    buildInfoOptions += BuildInfoOption.Traits("com.infare.scheduler.common.buildinfo.BuildInfoTrait")
  )

  val withTesting: Seq[Setting[_]] = Seq(
    parallelExecution in Test := false,
    fork in Test := true,
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
  )

  val withAssembly: Seq[Setting[_]] = baseAssemblySettings ++ Seq(
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "xml", xs@_*) => MergeStrategy.last
      case path if path.contains("commons-beanutils") || path.contains("commons-collections") => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    assemblyShadeRules in assembly ++= Seq(
      ShadeRule.rename("shapeless.**" -> "shaded_shapeless.@1").inAll,
      ShadeRule.rename("com.typesafe.config.**" -> "shaded_com.typesafe.config.@1").inAll
    ),
    assemblyJarName in assembly := "fat.jar",
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
    test in assembly := {}
  )

}
