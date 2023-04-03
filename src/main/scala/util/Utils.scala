package ru.starfish
package util

import better.files.{File, using}
import org.gitlab.api.models.GitlabBranch
import org.gitlab.api.query.ProjectsQuery

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.util.Try

object FileUtils {
  def bytesToFile(bytes: Array[Byte], path: String*): Boolean = {
    Files.createDirectories(Paths.get(path(0)))
    val bos = new BufferedOutputStream(new FileOutputStream(path.mkString("")))
    bos.write(bytes)
    bos.close()
    true
  }

  def flattenDir(copyTo: String, from: String): Unit = {
    val destination: File = Try(File(copyTo).createDirectory()).getOrElse(File(copyTo))
    File(from).children
      .flatMap(toFiles)
      .zipWithIndex
      .map(file => file._1.renameTo(file._2 + s"-${file._1.name}"))
      .foreach(_.copyToDirectory(destination))
  }

  def groupFilesByConnection(connections: mutable.Map[String, List[String]], copyFrom: String, copyTo: String): Unit =
    for ((key, value) <- connections) {
      Try {
        val destination = File(copyTo + "/" + key.stripSuffix(".java")).createDirectory()
        value.foreach(filepath => File(copyFrom + s"/$filepath").copyToDirectory(destination))
        File(copyFrom + s"/$key").copyToDirectory(destination)
      }
    }

  def parseConnectionsFromFile(connectionsFile: String): mutable.Map[String, List[String]] = {
    using(scala.io.Source.fromFile(connectionsFile)) { resource =>
      io.circe.parser
        .decode[mutable.Map[String, List[String]]](resource.getLines().mkString("\n"))
        .getOrElse(null)
    }
  }

  private def toFiles(file: File): List[File] =
    if (file.isDirectory) {
      file.children.flatMap(toFiles).toList
    } else {
      List(file)
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
