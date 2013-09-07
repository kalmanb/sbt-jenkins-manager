organization := "com.kalmanb"

name := "sbt-jenkins-manager"

version := "0.6.0"

sbtPlugin := true

publishMavenStyle := false

publishArtifact in Test := false

//publishTo := Some(Resolver.url("repo", new URL("http://"))(Resolver.ivyStylePatterns))

sbtVersion in Global := "0.13.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.RC1-SNAP4" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "org.scalaz" %% "scalaz-core" % "7.0.3", 
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "junit" % "junit" % "4.11" % "test"
)

