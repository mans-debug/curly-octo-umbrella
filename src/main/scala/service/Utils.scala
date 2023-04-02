package ru.starfish
package service

import better.files.File
import org.gitlab.api.models.GitlabBranch
import org.gitlab.api.query.ProjectsQuery

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.{Files, Paths}
import scala.util.Try

object FileUtils {
  def bytesToFile(bytes: Array[Byte], path: String*): Boolean = {
    Files.createDirectories(Paths.get(path(0)))
    val bos = new BufferedOutputStream(new FileOutputStream(path.mkString("")))
    bos.write(bytes)
    bos.close()
    true
  }

  def toFiles(file: File): List[File] = {
    if (file.isDirectory) {
      file.children.flatMap(toFiles).toList
    } else {
      List(file)
    }
  }

  def flattenDir(copyTo: String, from: String): Unit = {
    val dirPath = copyTo + "/flatten"
    val destination: File = Try(File(dirPath).createDirectory()).getOrElse(File(dirPath))
    File(from)
      .children
      .flatMap(toFiles)
      .zipWithIndex
      .map(file => file._1.renameTo(file._2 + s"-${file._1.name}"))
      .foreach(_.copyToDirectory(destination))
  }
}

class ProjectsQueryImproved extends ProjectsQuery {
  def isSearchNamespace(searchNamespaces: Boolean): ProjectsQueryImproved = {
    append("search_namespaces", searchNamespaces.toString)
    this
  }
}

object VersionOrdering extends Ordering[GitlabBranch] {
  def compare(x: GitlabBranch, y: GitlabBranch): Int = {
    val xVersions = toVersionList(x)
    val yVersions = toVersionList(y)
    for ((x, y) <- xVersions.zip(yVersions)) {
      if (x > y) {
        return 1
      } else if (y > x) {
        return -1;
      }
    }
    0
  }

  private def toVersionList(x: GitlabBranch) =
    x.getName
      .substring("release/".length)
      .split("\\.")
      .map(_.toInt)

}