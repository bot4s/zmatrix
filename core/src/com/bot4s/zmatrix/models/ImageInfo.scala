package com.bot4s.zmatrix.models

import zio.json._

case class ImageInfo(
  h: Option[Int] = None,
  w: Option[Int] = None,
  mimetype: Option[String] = None,
  size: Option[Int] = None,
  @jsonField("thumbnail_url") thumbnailUrl: Option[String] = None
)

object ImageInfo {
  implicit val imageinfoDecodetest: JsonDecoder[ImageInfo] = DeriveJsonDecoder.gen[ImageInfo]
  implicit val imageinfoencodetest: JsonEncoder[ImageInfo] = DeriveJsonEncoder.gen[ImageInfo]
}
