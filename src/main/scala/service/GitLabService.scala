package ru.starfish
package service

import FileUtils.bytesToFile

import com.typesafe.scalalogging.Logger
import org.gitlab.api.GitlabAPI
import org.gitlab.api.models.{GitlabBranch, GitlabProject, GitlabRepositoryFile, GitlabRepositoryTree}
import retry.Success

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Try


object GitLabService {
  private val log = Logger(getClass.getName)


  private val releaseRegex = """release/\d{1,2}\.\d{1,2}\.\d{1,2}"""

  def getLatestReleaseBranch(branches: List[GitlabBranch], project: GitlabProject): GitlabBranch =
    Try {
      branches
        .filter(_.getName.matches(releaseRegex))
        .sorted(VersionOrdering.reverse).head
    }.getOrElse {
      val branch = branches.find(_.getName.equals("develop"))
        .orElse(branches.find(_.getName.equals("master")))
        .getOrElse(branches.head)
      log.info(s"Have taken branch ${branch.getName} instead of release for project ${project.getName}")
      branch
    }


  def getGitlabProjectFiles(gitlabProject: GitlabProject,
                            branch: GitlabBranch)
                           (implicit api: GitlabAPI): List[Future[(GitlabRepositoryFile, GitlabProject)]] = {
    implicit val successPull: Success[(GitlabRepositoryFile, GitlabProject)] =
      Success[(GitlabRepositoryFile, GitlabProject)] { case (file, _) => file != null }

    def tryGettingRepoFile(tree: GitlabRepositoryTree): Future[(GitlabRepositoryFile, GitlabProject)] = {
      import retry.Backoff
      log.info(s"Trying to get ${gitlabProject.getName}:${tree.getPath}")
      Backoff(5, 1.second).apply(
        Future(
          try {
            (api.getRepositoryFile(gitlabProject, tree.getPath, branch.getCommit.getId), gitlabProject)
          } catch {
            case e: Exception => throw FailedDtoPull(gitlabProject.getName, tree.getPath, branch.getCommit.getId, e)
          }
        )
      )
    }

    api.getRepositoryTree(gitlabProject, null, null, true)
      .asScala
      .filter(tree => tree.getPath.contains("dto") && tree.getType.equals("blob"))
      .map(tryGettingRepoFile)
      .toList
  }


  def downloadGitlabRepoFile(repoFile: GitlabRepositoryFile,
                             project: GitlabProject,
                             destination: String)(implicit api: GitlabAPI): Boolean = {
    val bytes = api.getRawBlobContent(project, repoFile.getBlobId)
    log.info(s"Writing ${project.getName}:${repoFile.getFileName}")
    bytesToFile(bytes, destination, s"/${repoFile.getFileName}")
  }


  def coreProjects(implicit api: GitlabAPI): List[GitlabProject] = {
    val query = new ProjectsQueryImproved()
      .isSearchNamespace(true)
      .withSort("id")
      .withSort("asc")
      .withPerPage(50)
      .withSearch("cloud/core")
    api.getProjects(query).asScala.toList
  }
}
