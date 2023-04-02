package ru.starfish
package service

import better.files.File
import com.typesafe.scalalogging.Logger

import scala.Predef.->
import scala.collection.mutable
import scala.util.Using

object JaccardSim {
  private val log = Logger(getClass.getName)
  val bound = 68

  import scala.io.Source

  private def jaccardSimilarity(file1: String, file2: String): Double =
    Using.Manager { use =>
      val data1 = use(Source.fromFile(file1)).mkString
      val data2 = use(Source.fromFile(file2)).mkString
      val set1 = data1.split(" ").to(Set)
      val set2 = data2.split(" ").to(Set)
      val matchCount = (set1 & set2).size.toDouble
      val totalCount = (set1 | set2).size.toDouble
      matchCount / totalCount * 100
    }.getOrElse(0d)


  def findSimilarFiles(directory: String): mutable.Map[String, List[String]] = {
    val processed = collection.mutable.Map()[String, List[String]].withDefaultValue(List.empty[String])
    val files = File(directory).children
    for ((file, i) <- files.zipWithIndex) {
      log.info(s"Processing file #$i/${files.length}")
      for ((compareFile, j) <- files.zipWithIndex) {
        if (compareFile.path != file.path) {
          val matchPercent = jaccardSimilarity(file.path.toString, compareFile.path.toString)
          if (matchPercent > bound) {
            val key = file.name
            processed.put(key, compareFile.name :: processed(key))
          }
        }
      }
    }
    processed
  }
}
