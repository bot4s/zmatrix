package com.bot4s.zmatrix.models.responses

import zio.json._

final case class EventResponse(@jsonField("event_id") eventId: String)

object EventResponse {
  implicit val decoder: JsonDecoder[EventResponse] = DeriveJsonDecoder.gen
}
