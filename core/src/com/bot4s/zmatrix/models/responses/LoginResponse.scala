package com.bot4s.zmatrix.models.responses

import zio.json._

import com.bot4s.zmatrix.models._

final case class LoginResponse(
  @jsonField("user_id") userId: String,
  @jsonField("access_token") accessToken: AccessToken,
  @jsonField("home_server") homeServer: String,
  @jsonField("device_id") deviceId: String
)

object LoginResponse {
  implicit val decoder: JsonDecoder[LoginResponse] = DeriveJsonDecoder.gen
}
