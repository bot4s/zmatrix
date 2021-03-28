package com.bot4s.zmatrix.models

import zio.UIO
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import sttp.client3.RequestT

final case class AccessToken(token: String) extends AnyVal {
  def authenticateM[U[_], T, S](request: RequestT[U, T, S]): UIO[RequestT[U, T, S]] =
    UIO.succeed(request.auth.bearer(token))
}

object AccessToken {
  // We want to support both the derivation with the token field and also a flattened "anyval" decoding
  implicit val decoder: Decoder[AccessToken] =
    deriveConfiguredDecoder[AccessToken] or Decoder[String].map(x => AccessToken(x))
}
