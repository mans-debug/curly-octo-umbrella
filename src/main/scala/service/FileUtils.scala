package ru.starfish
package service

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.{Files, Paths}

object FileUtils {
  def bytesToFile(bytes: Array[Byte], path: String*): Boolean = {
    Files.createDirectories(Paths.get(path(0)))
    val bos = new BufferedOutputStream(new FileOutputStream(path.mkString("")))
    bos.write(bytes)
    bos.close()
    true
  }
}