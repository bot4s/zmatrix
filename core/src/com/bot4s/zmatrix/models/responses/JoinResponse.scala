package com.bot4s.zmatrix.models.responses

import io.circe.Decoder
import com.bot4s.zmatrix.models.StateDecoder._
import com.bot4s.zmatrix.models._
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class JoinResponse(
  roomId: RoomId
)

object JoinResponse {
  implicit val decoder: Decoder[JoinResponse] = deriveConfiguredDecoder
}
