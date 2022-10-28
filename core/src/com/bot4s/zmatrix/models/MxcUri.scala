package com.bot4s.zmatrix.models

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class MxcUri(contentUri: String) {
  val (serverName, mediaId) = {
    val parts = contentUri.replace("mxc://", "").split("/").toList
    parts match {
      case server :: media :: _ => (server, media)
      case _                    => ("", "")
    }
  }
}

object MxcUri {
  implicit val mxcuriDecoder: Decoder[MxcUri] = deriveConfiguredDecoder
}
