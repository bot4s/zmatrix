package com.bot4s.zmatrix.models

import zio.json._

object Visibility extends Enumeration {
  type Visibility = Value
  val publicRoom  = Value("public")
  val privateRoom = Value("private")

  implicit val encoder: JsonEncoder[Visibility.Value] = JsonEncoder[String].contramap(_.toString)
  implicit val decoder: JsonDecoder[Visibility.Value] = JsonDecoder[String].map(Visibility.withName)
}

object Preset extends Enumeration {
  type Preset = Value
  val privateChat        = Value("private_chat")
  val publicChat         = Value("public_chat")
  val trustedPrivateChat = Value("trusted_private_chat")

  implicit val encoder: JsonEncoder[Preset.Value] = JsonEncoder[String].contramap(_.toString)
  implicit val decoder: JsonDecoder[Preset.Value] = JsonDecoder[String].map(Preset.withName)
}

/**
 *  Documentation for the fields: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-createroom
 */
final case class RoomCreationData(
  visibility: Option[Visibility.Visibility] = None,
  @jsonField("room_alias_name") roomAliasName: Option[String] = None,
  name: Option[String] = None,
  topic: Option[String] = None,
  invite: Option[List[String]] = None,
  @jsonField("room_version") roomVersion: Option[String] = None,
  preset: Option[Preset.Preset] = None,
  @jsonField("is_direct") isDirect: Option[Boolean] = None
)

object RoomCreationData {

  implicit val roomCreationDataEncoder: JsonEncoder[RoomCreationData] = DeriveJsonEncoder.gen[RoomCreationData]
  implicit val roomCreationDataDecoder: JsonDecoder[RoomCreationData] = DeriveJsonDecoder.gen[RoomCreationData]
}
