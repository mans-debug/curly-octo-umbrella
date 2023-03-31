ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "ObjectTransferStorage",
    idePackagePrefix := Some("ru.starfish")
  )
scalacOptions += "-Ymacro-annotations"
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.8.8",
  "com.softwaremill.sttp.client3" %% "circe" % "3.8.8",
  "io.circe" %% "circe-core" % "0.14.3",
  "io.circe" %% "circe-generic" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.3",
  "io.circe" %% "circe-generic-extras" % "0.14.3",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "org.gitlab" % "java-gitlab-api" % "4.1.1",
)