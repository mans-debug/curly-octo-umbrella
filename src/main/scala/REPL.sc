import better.files.File
import java.nio.file.StandardCopyOption

val resources = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources"
val destination = File("/Users/mansurminnikaev/PycharmProjects/object-transfer/copy")
implicit val copyOption: File.CopyOptions = Seq(StandardCopyOption.REPLACE_EXISTING)
def toFiles(file: File): List[File] = {
  if (file.isDirectory) {
    file.children.flatMap(toFiles).toList
  } else {
    List(file)
  }
}
destination.children.filter(x => !x.name.endsWith(".java")).foreach(println)
File(resources)
  .children
  .flatMap(toFiles)
  .zipWithIndex
  .map(file => file._1.renameTo(file._2 + s"-${file._1.name}"))
  .foreach(_.copyToDirectory(destination))




