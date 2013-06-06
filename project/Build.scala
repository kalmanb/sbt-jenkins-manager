import sbt._
import Keys._

object sbtJenkins extends Build {
  val projectName = "sbt-jenkins-manager"
  val buildVersion = "0.1.0-SNAPSHOT"

  override lazy val settings = super.settings ++ Seq(resolvers := Seq())

  //val slf4j = "org.slf4j" % "slf4j-api" % "1.7.5"
  val scalaTest = "org.scalatest" %% "scalatest" % "2.0.M6-SNAP3" % "test"
  val junit = "junit" % "junit" % "4.11" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"

  val publishedScalaSettings = Seq(
    scalaVersion := "2.9.2",
    //crossScalaVersions := Seq("2.9.1", "2.9.2"),
    sbtPlugin := true,
    publishMavenStyle := false,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    //scalaVersion := "2.10.1",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
      //"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
     ),

    libraryDependencies ++= Seq(
      "net.databinder.dispatch" % "dispatch-core_2.9.2" % "0.9.5",
//     Testing Libs
      scalaTest,
      junit,
      mockito)
    )

  lazy val root = Project(
    id = projectName,
    base = file("."),
    settings = Seq(
      organization := "org.kalmanb",
      version := buildVersion
    ) ++ Project.defaultSettings ++ publishedScalaSettings)

}

