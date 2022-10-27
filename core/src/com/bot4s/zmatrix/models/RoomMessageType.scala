package com.bot4s.zmatrix.models

import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import io.circe.{ Decoder, DecodingFailure, Encoder, Json }

// https://spec.matrix.org/latest/client-server-api/#mroommessage-msgtypes
sealed trait RoomMessageType {
  def msgtype: String
}

object RoomMessageType {

  final case class RoomMessageTextContent(
    body: String,
    format: Option[String] = None,
    formattedBody: Option[String] = None
  ) extends RoomMessageType {
    val msgtype: String = "m.text"
  }

  final case class RoomMessageImageContent(
    body: String,
    url: Option[String] = None,
    info: Option[ImageInfo] = None
  ) extends RoomMessageType {
    val msgtype: String = "m.image"
  }

  implicit val textContentEncoder: Encoder[RoomMessageTextContent] = Encoder.instance { textContent =>
    val innerTextContentEncoder = deriveConfiguredEncoder[RoomMessageTextContent]
    textContent.asJson(innerTextContentEncoder).deepMerge(Json.obj(("msgtype", Json.fromString(textContent.msgtype))))
  }
  implicit val textContentDecoder: Decoder[RoomMessageTextContent] = deriveConfiguredDecoder

  implicit val imageContentEncoder: Encoder[RoomMessageImageContent] = Encoder.instance { imgContent =>
    val innerImageContentEncoder = deriveConfiguredEncoder[RoomMessageImageContent]
    imgContent.asJson(innerImageContentEncoder).deepMerge(Json.obj(("msgtype", Json.fromString(imgContent.msgtype))))
  }
  implicit val imageContentDecoder: Decoder[RoomMessageImageContent] = deriveConfiguredDecoder

  implicit val messageTypeEncoder: Encoder[RoomMessageType] = Encoder.instance {
    case text: RoomMessageTextContent => text.asJson
    case img: RoomMessageImageContent => img.asJson
  }
  implicit val messageTypeDecoder: Decoder[RoomMessageType] = c =>
    c.downField("msgtype").as[String].flatMap {
      case "m.text"  => c.as[RoomMessageTextContent]
      case "m.image" => c.as[RoomMessageImageContent]
      case msgtype   => Left(DecodingFailure(s"$msgtype is not supported", Nil))
    }
}
