package ru.starfish

import service.FileUtils

import better.files.File

import java.nio.file.Paths

object Flatten extends App {
  /*val baseDir = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources/gitlab-copy"
  val copyTo = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage"

  FileUtils.flattenDir(copyTo,baseDir)*/

  println(File("/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/flatten").path.toString)

}
