package ru.starfish
import io.circe.syntax._
import ru.starfish.service.JaccardSim
object JaccardTest extends App {
  val path = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/flatten"
  val json = JaccardSim.findSimilarFiles(path).asJson.noSpaces
  println(json)
}
