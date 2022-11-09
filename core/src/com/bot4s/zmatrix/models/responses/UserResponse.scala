package com.bot4s.zmatrix.models.responses

import zio.json._

final case class UserResponse(@jsonField("user_id") userId: String)

object UserResponse {
  implicit val decoder: JsonDecoder[UserResponse] = DeriveJsonDecoder.gen
}
