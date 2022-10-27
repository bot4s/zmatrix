package com.bot4s.zmatrix.models.responses

import com.bot4s.zmatrix.models._
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class LoginResponse(
  userId: String,
  accessToken: AccessToken,
  homeServer: String,
  deviceId: String
)

object LoginResponse {
  implicit val decoder: Decoder[LoginResponse] = deriveConfiguredDecoder
}
