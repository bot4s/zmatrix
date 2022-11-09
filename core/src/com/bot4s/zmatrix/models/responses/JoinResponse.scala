package com.bot4s.zmatrix.models.responses

import zio.json._

import com.bot4s.zmatrix.models._

final case class JoinResponse(
  @jsonField("room_id") roomId: RoomId
)

object JoinResponse {
  implicit val decoder: JsonDecoder[JoinResponse] = DeriveJsonDecoder.gen
}
