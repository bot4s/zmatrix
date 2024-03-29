package com.bot4s.zmatrix

import com.bot4s.zmatrix.models._
import sttp.client3._
import sttp.model.MediaType

object ImageMessage extends ExampleApp[Unit] {

  val upload = Matrix
    .upload(
      uri"https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Cc.logo.circle.svg/1024px-Cc.logo.circle.png",
      Some(MediaType.ImagePng)
    )

  val runExample =
    (for {
      up <- upload
      _ <- Matrix.sendEvent(
             RoomId(""), // replace with a room that exists
             RoomMessageType.RoomMessageImageContent(
               "CC Logo",
               url = Some(up.contentUri),
               info = Some(ImageInfo(w = Some(100), h = Some(100)))
             )
           )
    } yield ()).debug.refineOrDie { case x: MatrixError => x }.unit

}
