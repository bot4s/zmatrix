package com.bot4s.zmatrix.models

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class AccessToken(token: String) extends AnyVal

object AccessToken {
  // We want to support both the derivation with the token field and also a flattened "anyval" decoding
  implicit val decoder: Decoder[AccessToken] =
    deriveConfiguredDecoder[AccessToken] or Decoder[String].map(x => AccessToken(x))
}
