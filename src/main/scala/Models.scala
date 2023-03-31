package ru.starfish

import Main.config

import io.circe.generic.extras._
import org.gitlab.api.models.GitlabBranch

@ConfiguredJsonCodec case class Links(@JsonKey("repo_branches") branches: String)

@ConfiguredJsonCodec case class Project(id: Int,
                                        name: String,
                                        @JsonKey("path_with_namespace") path: String,
                                        @JsonKey("_links") links: Links)

@ConfiguredJsonCodec case class Branch(name: String, commit: Commit)

@ConfiguredJsonCodec case class Commit(id: String)

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
//      .map(digit => if (digit.length == 1) digit + "0" else digit)
      .map(_.toInt)

}