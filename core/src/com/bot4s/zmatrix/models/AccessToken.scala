package com.bot4s.zmatrix.models

import zio.json._

final case class AccessToken(token: String) extends AnyVal

object AccessToken {
  // We want to support both the derivation with the token field and also a flattened "anyval" decoding
  implicit val roomIdDecoder: JsonDecoder[AccessToken] =
    DeriveJsonDecoder.gen[AccessToken] orElse JsonDecoder[String].map(AccessToken.apply)
}
