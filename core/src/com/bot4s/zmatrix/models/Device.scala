package com.bot4s.zmatrix.models

import zio.json._

final case class Device(
  @jsonField("device_id") deviceId: String,
  @jsonField("display_name") displayName: Option[String],
  @jsonField("last_seen_ip") lastSeenIp: Option[String],
  @jsonField("last_seen_ts") lastSeenTs: Option[Long]
)

object Device {
  implicit val decoder: JsonDecoder[Device] = DeriveJsonDecoder.gen
}
