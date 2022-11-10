package com.bot4s.zmatrix.models

import zio.json._

final case class RoomId(id: String) extends AnyVal

object RoomId {

  implicit val roomIdDecoder: JsonDecoder[RoomId] =
    JsonDecoder[String].map(RoomId.apply)

  implicit val roomIdEncoder: JsonEncoder[RoomId] =
    JsonEncoder.string.contramap[RoomId](_.id)

  implicit val roomIdKeyEncoder: JsonFieldEncoder[RoomId] =
    JsonFieldEncoder.string.contramap[RoomId](_.id)

  implicit val roomIdKeyDecoder: JsonFieldDecoder[RoomId] =
    JsonFieldDecoder.string.map(RoomId.apply)

}
