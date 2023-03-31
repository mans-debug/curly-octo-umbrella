package ru.starfish

import better.files.File

object Connections extends App {

  def getFile(prefix: String, filename: String): File = File(prefix + "/" + filename)

  val connectionsFile = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources/connections.json"
  val json = scala.io.Source.fromFile(connectionsFile).getLines().mkString("\n")
  val obj = io.circe.parser.decode[Map[String, List[String]]](json).getOrElse(null)
  val copyFrom = "/Users/mansurminnikaev/PycharmProjects/object-transfer/copy"
  val copyTo = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources/groupped"
  for ((key, value) <- obj) {
    if (key != "path") {
      val destination = File(copyTo + "/" + key.stripSuffix(".java")).createDirectory()
      value.foreach(filepath => File(copyFrom + s"/$filepath").copyToDirectory(destination))
      File(copyFrom + s"/$key").copyToDirectory(destination)
    }
  }
  val x = 4
}
