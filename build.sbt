val finagleVersion = "18.7.0"
val linkerdVersion = "1.5.0"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.homeaway",
      scalaVersion := "2.12.7",
      version := "0.1.0-SNAPSHOT"
    )),
  name := "svc2svc-stats",
  resolvers := Seq(
    Resolver.mavenLocal,
    Resolver.defaultLocal
  ),
  libraryDependencies ++= Seq(
    "com.twitter"    %% "finagle-http"          % finagleVersion % "provided",
    "com.twitter"    %% "util-core"             % finagleVersion % "provided",
    "io.buoyant"     %% "linkerd-core"          % linkerdVersion % "provided",
    "io.buoyant"     %% "linkerd-protocol-http" % linkerdVersion % "provided",
    "org.scalatest"  %% "scalatest"             % "3.0.5" % Test,
    "org.scalacheck" %% "scalacheck"            % "1.13.5" % Test
  ),
  scalacOptions += "-Ypartial-unification",
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
)
