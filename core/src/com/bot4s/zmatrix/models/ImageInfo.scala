package com.bot4s.zmatrix.models

import io.circe.generic.extras.semiauto.{ deriveConfiguredDecoder, deriveConfiguredEncoder }
import io.circe.{ Decoder, Encoder }

case class ImageInfo(
  h: Int,
  w: Int,
  mimetype: String,
  size: Int,
  thumbnailUrl: Option[String]
)

object ImageInfo {
  implicit val imageInfoEncoder: Encoder[ImageInfo] = deriveConfiguredEncoder
  implicit val imageInfoDecoder: Decoder[ImageInfo] = deriveConfiguredDecoder
}
