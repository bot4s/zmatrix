package com.bot4s.zmatrix

import zio.Console.printLine
import zio.ZIO

import com.bot4s.zmatrix.api.media
import sttp.client3._
import sttp.model.MediaType

object Upload extends ExampleApp[Unit] {

  val upload = media
    .upload(
      uri"https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Cc.logo.circle.svg/1024px-Cc.logo.circle.png",
      MediaType.ImagePng
    )

  val runExample =
    (for {
      up     <- upload.debug
      config <- ZIO.service[MatrixConfiguration].flatMap(_.get)
      _ <- printLine(
             s"Media uploaded, check out ${config.matrix.mediaApi}/download/${up.serverName}/${up.mediaId}"
           )
    } yield ()).debug.refineOrDie { case x: MatrixError => x }.unit

}
