package ru.starfish
package config

import better.files.File

import scala.util.Try

object Constants {
  private val ORIGIN_DIR = "/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources"

  private val BASE_DIR = ORIGIN_DIR + "/base"
  val GITLAB_PROJECT_DTO: String = BASE_DIR + "/gitlab"
  val FLATTENED_DTO: String = BASE_DIR + "/flatten"
  val GROUPED_DTO: String = BASE_DIR + "/grouped"

  val GITLAB_URL = "https://starfish.gitlab.yandexcloud.net"
  val GITLAB_PRIVATE_TOKEN = "glpat-AD2FbLwuFx2FEgNzYN8t"

  Try(File(BASE_DIR).createDirectory())
  Try(File(GITLAB_PROJECT_DTO).createDirectory())
  Try(File(FLATTENED_DTO).createDirectory())
  Try(File(GROUPED_DTO).createDirectory())
}
