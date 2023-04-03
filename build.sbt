ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "ObjectTransferStorage",
    idePackagePrefix := Some("ru.starfish")
  )
libraryDependencies ++= Seq(
  "io.circe" %% "circe-parser" % "0.14.3",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "org.gitlab" % "java-gitlab-api" % "4.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.3.5",
  "com.softwaremill.retry" %% "retry" % "0.3.6"
)