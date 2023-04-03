package ru.starfish
package service

import better.files.File
import com.typesafe.scalalogging.Logger

import scala.collection.{immutable, mutable}
import scala.util.Using

object JaccardSimilarity {
  private val log = Logger(getClass.getName)
  val bound = 80

  import scala.io.Source

  private def jaccardSimilarity(file1: String, file2: String): Double =
    Using
      .Manager { use =>
        val data1 = use(Source.fromFile(file1)).mkString
        val data2 = use(Source.fromFile(file2)).mkString
        val set1 = immutable.Set(data1.split("\\s"): _*)
        val set2 = immutable.Set(data2.split("\\s"): _*)
        val matchCount = (set1 & set2).size.toDouble
        val totalCount = (set1 | set2).size.toDouble
        matchCount / totalCount * 100
      }
      .getOrElse(0d)

  def findSimilarFiles(directory: String): mutable.Map[String, List[String]] = {
    val files = File(directory).children.toList
    val processed = collection.mutable.Map[String, List[String]]()
    files.foreach(file => processed.put(file.name, Nil))
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
