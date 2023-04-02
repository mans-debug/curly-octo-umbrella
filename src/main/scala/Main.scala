package ru.starfish

import service.FailedDtoPull
import service.GitLabService._

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto
import io.circe.generic.extras._
import org.gitlab.api.models.{GitlabProject, GitlabRepositoryFile}
import org.gitlab.api.{GitlabAPI, TokenType}

import scala.collection.{SeqView, View}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

object Main {
  private val log = Logger(getClass.getName)

  val baseDir = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources/gitlab"

  private val gitlabUrl = "https://starfish.gitlab.yandexcloud.net"
  private val privateToken = "glpat-AD2FbLwuFx2FEgNzYN8t"
  implicit val config: Configuration = Configuration.default
  implicit val api: GitlabAPI = GitlabAPI.connect(gitlabUrl, privateToken, TokenType.PRIVATE_TOKEN)

  def main(args: Array[String]): Unit = {
    log.info("Pulling core gitlab projects")
    val projects = coreProjects
    val futureDtos: List[Future[(GitlabRepositoryFile, GitlabProject)]] = for (
      (project, i) <- projects.zipWithIndex;
      _ = log.info(s"Processing project with name ${project.getName}\t\t#$i/${projects.size}");
      branches = api.getBranches(project).asScala.to(List);
      _ = log.info("Pulled project branches");
      latestReleaseBranch = getLatestReleaseBranch(branches, project);
      _ = log.info(s"Latest release branch ${latestReleaseBranch.getName}");
      dto <- getGitlabProjectFiles(project, latestReleaseBranch)
    ) yield dto

    val downloadDtoFuture: SeqView[Future[Boolean]] = futureDtos.view.map(_.transform(downloadOnSuccess).recover({case _: Throwable => false}))
    Await.result(Future.sequence(downloadDtoFuture), Inf)

    println(downloadDtoFuture.mkString(sep = "\n"))
  }

  private def downloadOnSuccess(attempt: Try[(GitlabRepositoryFile, GitlabProject)]) =
    attempt match {
      case Success((repoFile, project)) =>
        Try {
          val destination = baseDir + s"/${project.getName}";
          downloadGitlabRepoFile(repoFile, project, destination)
        }
      case Failure(ex: FailedDtoPull) =>
        log.error(ex.getMessage)
        Failure(ex)
      case Failure(ex) => Failure(ex)
    }

  private def hasFailed[T](opt: Option[Try[T]]) =
    if (opt.isEmpty) false
    else {
      opt.get match {
        case Failure(e) => true
        case _ => false
      }
    }
}
