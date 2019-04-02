name := "cloud-fun"

version := "0.1"

scalaVersion := "2.12.8"


val root = project.in(file("."))
  .settings(name := "cloud-fun")
  .aggregate(downloader)

lazy val downloader = project.in(file("downloader"))
  .settings(Settings.withCommonSettings: _*)
  .settings(Settings.withAssembly: _*)
  .settings(Settings.withTesting: _*)
  .settings(inThisBuild(libraryDependencies ++= Settings.configs ++ Settings.deps))

