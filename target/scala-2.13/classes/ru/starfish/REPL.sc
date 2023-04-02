import retry.{Backoff, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

/*
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
//destination.children.filter(x => !x.name.endsWith(".java")).foreach(println)
/*File(resources)
  .children
  .flatMap(toFiles)
  .zipWithIndex
  .map(file => file._1.renameTo(file._2 + s"-${file._1.name}"))
  .foreach(_.copyToDirectory(destination))*/




*/


def return1: Int =  throw new RuntimeException()
implicit val intSuccess: Success[Int] = Success[Int](_ == 1)
val x: Future[Int] = retry.Backoff(2, 1.seconds).apply(Future(return1))
Thread.sleep(20_000)
println(x.value)