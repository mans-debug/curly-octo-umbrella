package ru.starfish

import io.circe.generic.auto
import io.circe.generic.extras._
import org.gitlab.api.models.{GitlabBranch, GitlabProject, GitlabRepositoryFile, GitlabRepositoryTree}
import org.gitlab.api.{GitlabAPI, TokenType}
import sttp.client3._

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Random, Try}

object Main {
  val backend = HttpClientSyncBackend()
  val baseDir = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources"
  val api = GitlabAPI.connect("https://starfish.gitlab.yandexcloud.net", "glpat-EWiZsx1NkCDcGvxFZWrV", TokenType.PRIVATE_TOKEN)


  def getLatestReleaseBranch(branches: collection.mutable.Buffer[GitlabBranch], project: GitlabProject): GitlabBranch = {
    val regex = """release/\d{1,2}\.\d{1,2}\.\d{1,2}"""
    Try {
      branches
        .filter(_.getName.matches(regex))
        .sorted(VersionOrdering.reverse)(0)
    }.getOrElse {
      {
        val branch = branches.find(_.getName.equals("develop"))
          .orElse(branches.find(_.getName.equals("master")))
          .getOrElse(branches(0))
        println(s"Took another branch for project ${project.getName} with name ${branch.getName}")
        branch
      }
    }
  }

  def bytesToFile(bytes: Array[Byte], destination: String*): Boolean = {
    Files.createDirectories(Paths.get(destination(0)))
    val bos = new BufferedOutputStream(new FileOutputStream(destination.mkString("")))
    bos.write(bytes)
    bos.close()
    true
  }

  def downloadProject(gitlabProject: GitlabProject, branch: GitlabBranch, destination: String) =
    api.getRepositoryTree(gitlabProject, null, null, true)
      .asScala
      .view
      .filter(tree => tree.getPath.contains("dto") && tree.getType.equals("blob"))
      .map(tree => Future(retry(gitlabProject, branch, tree)))
      .map(futureFile => {
        futureFile
          .onComplete {
            attempt =>
              if (attempt.isSuccess) {
                val gitFile = attempt.get
                println(s"Processing file ${gitFile.getFileName} ${gitFile.getFilePath}")
                val bytes = api.getRawBlobContent(gitlabProject, gitFile.getBlobId)
                bytesToFile(bytes, destination, s"/${gitFile.getFileName}")
              } else {
                println(s"Could not process file for ${gitlabProject.getName} project")
              }
          }
      })
      .toList

  private def retry(gitlabProject: GitlabProject, branch: GitlabBranch, tree: GitlabRepositoryTree) = {
    var count = 0
    var flag = false
    var res: GitlabRepositoryFile = null;
    do {
      if (count > 400) {
        println(s"booo too much, do it urself project ${gitlabProject.getName} branch ${branch.getName} tree ${tree.getName} and path ${tree.getPath}")
        Thread.sleep(1000 * Random.between(1, 4))
      }
      if (count > 1) {
        println(s"Attempt #$count")
      }
      try {
        count += 1
        res = api.getRepositoryFile(gitlabProject, tree.getPath, branch.getCommit.getId)
        flag = false
      } catch {
        case e: Exception => flag = true
      }
    } while (flag)
    if (count > 1) {
      println(s"Finally, success after $count attempts")
    }
    res
  }

  def main(args: Array[String]): Unit = {
    val projects = getCoreProjects()
    for (projectZipped <- projects.zipWithIndex) {
      val (project, i) = projectZipped
      val destination = baseDir + s"/${project.getName}"
      println(s"\tStarted processing project with name ${project.getName}\t\t#$i/${projects.size}")
      val branches = api.getBranches(project).asScala
      val latestReleaseBranch = getLatestReleaseBranch(branches, project)
      val file = downloadProject(project, latestReleaseBranch, destination)
      if (file.isEmpty) {
        println(s"Could not find dto folder for project ${project.getName}")
      }
      println(s"Finished processing ${project.getName}")
    }
    Thread.sleep(3_000_000_000L)
  }

  implicit val config: Configuration = Configuration.default


  def getCoreProjects(): collection.mutable.Buffer[GitlabProject] = {
    val query = new ProjectsQueryImproved()
      .isSearchNamespace(true)
      .withSort("id")
      .withSort("asc")
      .withPerPage(50)
      .withSearch("cloud/core")
    api.getProjects(query).asScala
  }


}