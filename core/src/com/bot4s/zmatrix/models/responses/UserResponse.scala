package com.bot4s.zmatrix.models.responses

import io.circe.Decoder
import com.bot4s.zmatrix.models._
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class UserResponse(userId: String)

object UserResponse {
  implicit val decoder: Decoder[UserResponse] = deriveConfiguredDecoder
}
