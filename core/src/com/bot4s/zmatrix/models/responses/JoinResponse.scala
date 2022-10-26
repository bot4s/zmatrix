package com.bot4s.zmatrix.models.responses

import com.bot4s.zmatrix.models._
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class JoinResponse(
  roomId: RoomId
)

object JoinResponse {
  implicit val decoder: Decoder[JoinResponse] = deriveConfiguredDecoder
}
