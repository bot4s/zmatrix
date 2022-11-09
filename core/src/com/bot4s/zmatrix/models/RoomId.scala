package com.bot4s.zmatrix.models

import zio.json._

final case class RoomId(id: String) extends AnyVal

object RoomId {

  implicit val roomIdDecoder: JsonDecoder[RoomId] =
    DeriveJsonDecoder.gen[RoomId] orElse JsonDecoder[String].map(RoomId.apply)

  implicit val roomIdEncoder: JsonEncoder[RoomId] =
    DeriveJsonEncoder.gen[RoomId]

  implicit val roomIdKeyEncoder: JsonFieldEncoder[RoomId] =
    JsonFieldEncoder.string.contramap[RoomId](_.id)

  implicit val roomIdKeyDecoder: JsonFieldDecoder[RoomId] =
    JsonFieldDecoder.string.map(RoomId.apply)

}
