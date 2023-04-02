package ru.starfish
package service

case class FailedDtoPull(projectName: String,
                         filepath: String,
                         commit: String,
                         e: Throwable) extends RuntimeException {
  override def getMessage: String = s"Could not pull repo file for $projectName/$commit:$filepath"
}
