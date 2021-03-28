package com.bot4s.zmatrix.models

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class Device(
  deviceId: String,
  displayName: Option[String],
  lastSeenIp: Option[String],
  lastSeenTs: Option[Long]
)

object Device {
  implicit val decoder: Decoder[Device] = deriveConfiguredDecoder
}
