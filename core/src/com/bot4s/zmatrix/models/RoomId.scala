package com.bot4s.zmatrix.models

import io.circe.generic.extras.semiauto._
import io.circe.{ Decoder, Encoder, KeyDecoder }

final case class RoomId(id: String) extends AnyVal

object RoomId {

  implicit val roomIdDecoder: Decoder[RoomId] =
    deriveConfiguredDecoder[RoomId] or Decoder[String].map(x => RoomId(x))

  implicit val roomIdEncoder: Encoder[RoomId] =
    deriveConfiguredEncoder[RoomId]

  implicit val roomIdKeyDecoder: KeyDecoder[RoomId] =
    key => Some(RoomId(key))

}
