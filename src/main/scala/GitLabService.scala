package ru.starfish

import org.gitlab.api.models.{GitlabBranch, GitlabProject}

import scala.util.Try

class GitLabService {

  private val releaseRegex = """release/\d{1,2}\.\d{1,2}\.\d{1,2}"""
  def getLatestReleaseBranch(branches: collection.mutable.Buffer[GitlabBranch], project: GitlabProject): GitlabBranch = {
    Try {
      branches
        .filter(_.getName.matches(releaseRegex))
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
}
