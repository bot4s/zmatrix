package com.bot4s.zmatrix.models

import zio.json._

final case class MxcUri(@jsonField("content_uri") contentUri: String) {
  val (serverName, mediaId) = {
    val parts = contentUri.replace("mxc://", "").split("/").toList
    parts match {
      case server :: media :: _ => (server, media)
      case _                    => ("", "")
    }
  }
}

object MxcUri {
  implicit val mxcuriDecoder: JsonDecoder[MxcUri] = DeriveJsonDecoder.gen
}
