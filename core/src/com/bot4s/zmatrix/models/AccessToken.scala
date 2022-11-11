package com.bot4s.zmatrix.models

import zio.json._

final case class AccessToken(token: String) extends AnyVal

object AccessToken {
  implicit val roomIdDecoder: JsonDecoder[AccessToken] = JsonDecoder[String].map(AccessToken.apply)
}
