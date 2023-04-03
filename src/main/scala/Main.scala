package ru.starfish

import config.Constants._
import exceptions.FailedDtoPull
import service.GitLabService._
import service.JaccardSimilarity
import util.FileUtils

import com.typesafe.scalalogging.Logger
import org.gitlab.api.models.{GitlabProject, GitlabRepositoryFile}
import org.gitlab.api.{GitlabAPI, TokenType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

object Main {
  private val log = Logger(getClass.getName)
  implicit val api: GitlabAPI = GitlabAPI.connect(GITLAB_URL, GITLAB_PRIVATE_TOKEN, TokenType.PRIVATE_TOKEN)

  def main(args: Array[String]): Unit = {
    log.info("Pulling core gitlab projects")
    val projects = coreProjects
    val futureDtos: List[Future[(GitlabRepositoryFile, GitlabProject)]] = getGitlabFiles(projects)

    val downloadedFutures = futureDtos
      .map(
        _.transform(downloadOnSuccess)
          .recover({ case _: Throwable => false })
      )
    log.info("Waiting for files to download")
    Await.result(Future.sequence(downloadedFutures), Inf)
    log.info("Flattening project files")
    FileUtils.flattenDir(FLATTENED_DTO, GITLAB_PROJECT_DTO)
    log.info("Searching for similar files")
    val connections = JaccardSimilarity.findSimilarFiles(FLATTENED_DTO)
    log.info("Packing similar files together")
    FileUtils.groupFilesByConnection(connections, FLATTENED_DTO, GROUPED_DTO)
    log.info("Job finished successfully")
  }

  private def getGitlabFiles(projects: List[GitlabProject]) = {
    for (
      (project, i) <- projects.zipWithIndex;
      _ = log.info(s"Processing project with name ${project.getName}\t\t#$i/${projects.size}");
      branches = api.getBranches(project).asScala.to(List);
      _ = log.info("Pulled project branches");
      latestReleaseBranch = getLatestReleaseBranch(branches, project);
      _ = log.info(s"Latest release branch ${latestReleaseBranch.getName}");
      dto <- getGitlabProjectFiles(project, latestReleaseBranch)
    ) yield dto
  }

  private def downloadOnSuccess(attempt: Try[(GitlabRepositoryFile, GitlabProject)]) =
    attempt match {
      case Success((repoFile, project)) =>
        Try {
          val destination = GITLAB_PROJECT_DTO + s"/${project.getName}";
          downloadGitlabRepoFile(repoFile, project, destination)
        }
      case Failure(ex: FailedDtoPull) =>
        log.error(ex.getMessage)
        Failure(ex)
      case Failure(ex) => Failure(ex)
    }

}
