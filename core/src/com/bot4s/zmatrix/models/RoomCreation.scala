package com.bot4s.zmatrix.models
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{ deriveConfiguredDecoder, deriveConfiguredEncoder }
import io.circe.{ Decoder, Encoder }

object Visibility extends Enumeration {
  type Visibility = Value
  val publicRoom  = Value("public")
  val privateRoom = Value("private")

  implicit val visibilityDecoder: Decoder[Visibility.Value] = Decoder.decodeEnumeration(Visibility)
  implicit val visibilityEncoder: Encoder[Visibility.Value] = Encoder.encodeEnumeration(Visibility)
}

object Preset extends Enumeration {
  type Preset = Value
  val privateChat        = Value("private_chat")
  val publicChat         = Value("public_chat")
  val trustedPrivateChat = Value("trusted_private_chat")

  implicit val presetDecoder: Decoder[Preset.Value] = Decoder.decodeEnumeration(Preset)
  implicit val presetEncoder: Encoder[Preset.Value] = Encoder.encodeEnumeration(Preset)
}

/**
 *  Documentation for the fields: https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-createroom
 */
final case class RoomCreationData(
  visibility: Option[Visibility.Visibility] = None,
  roomAliasName: Option[String] = None,
  name: Option[String] = None,
  topic: Option[String] = None,
  invite: Option[List[String]] = None,
  roomVersion: Option[String] = None,
  preset: Option[Preset.Preset] = None,
  isDirect: Option[Boolean] = None
)

object RoomCreationData {
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val roomCreationDataEncoder: Encoder[RoomCreationData] = deriveConfiguredEncoder
  implicit val roomCreationDataDecoder: Decoder[RoomCreationData] = deriveConfiguredDecoder
}
