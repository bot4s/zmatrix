package com.bot4s.zmatrix.models

import io.circe.generic.extras.semiauto.{ deriveConfiguredDecoder, deriveConfiguredEncoder }
import io.circe.{ Decoder, Encoder }

case class ImageInfo(
  h: Option[Int] = None,
  w: Option[Int] = None,
  mimetype: Option[String] = None,
  size: Option[Int] = None,
  thumbnailUrl: Option[String] = None
)

object ImageInfo {
  implicit val imageInfoEncoder: Encoder[ImageInfo] = deriveConfiguredEncoder
  implicit val imageInfoDecoder: Decoder[ImageInfo] = deriveConfiguredDecoder
}
