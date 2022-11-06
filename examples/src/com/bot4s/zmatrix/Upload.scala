package com.bot4s.zmatrix

import zio.Console.printLine
import zio.ZIO

import java.io.File

import sttp.client3._

object Upload extends ExampleApp[Unit] {

  val remoteUpload = Matrix
    .upload(
      uri"https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Cc.logo.circle.svg/1024px-Cc.logo.circle.png",
      None
    )

  val localUpload = Matrix
    .upload(
      new File("/tmp/194031-2.jpg"),
      None
    )

  val runExample =
    (for {
      up     <- remoteUpload
      config <- ZIO.service[MatrixConfiguration].flatMap(_.get)
      _ <- printLine(
             s"Media uploaded, check out ${config.matrix.mediaApi}/download/${up.serverName}/${up.mediaId}"
           )
    } yield ()).debug.refineOrDie { case x: MatrixError => x }.unit

}
