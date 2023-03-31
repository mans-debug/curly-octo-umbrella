import io.circe._, io.circe.parser._
val connectionsFile = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources/connections.json"
val json = scala.io.Source.fromFile(connectionsFile).getLines().mkString("\n")
val obj: Json = parse(json).right.get
val copyFrom = "/Users/mansurminnikaev/PycharmProjects/object-transfer/copy"
for (key <- obj.) {
  println(key)
}
val x = 4