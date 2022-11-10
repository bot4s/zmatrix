package com.bot4s.zmatrix.models

import zio.json._
import zio.json.ast._

// https://spec.matrix.org/latest/client-server-api/#mroommessage-msgtypes
sealed trait RoomMessageType {
  def msgtype: String
}

object RoomMessageType {

  // redaction/deleted messages
  case object RoomMessageEmpty extends RoomMessageType {
    val msgtype = ""
  }

  final case class RoomMessageTextContent(
    body: String,
    format: Option[String] = None,
    @jsonField("formatted_body") formattedBody: Option[String] = None
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

  // I can't think of a reason for the inner `toJsonAst` call to fail, but we still default to an empty object
  implicit val messageTypeEncoder: JsonEncoder[RoomMessageType] = Json.encoder.contramap[RoomMessageType] {
    case text: RoomMessageTextContent    => text.toJsonAST.getOrElse(Json.Obj())
    case img: RoomMessageImageContent    => img.toJsonAST.getOrElse(Json.Obj())
    case redacted: RoomMessageEmpty.type => redacted.toJsonAST.getOrElse(Json.Obj())
  }

  implicit val roomTextContentDecoder: JsonDecoder[RoomMessageTextContent]   = DeriveJsonDecoder.gen
  implicit val roomImageContentDecoder: JsonDecoder[RoomMessageImageContent] = DeriveJsonDecoder.gen

  implicit val roomMessageTypeDecoder: JsonDecoder[RoomMessageType] = JsonDecoder[Json].mapOrFail { json =>
    json.get(JsonCursor.field("msgtype")).flatMap(_.as[String]).left.flatMap(_ => Right("")).flatMap {
      case "m.text"  => json.as[RoomMessageTextContent]
      case "m.image" => json.as[RoomMessageImageContent]
      case ""        => Right[String, RoomMessageType](RoomMessageEmpty)
      case msgtype   => Left[String, RoomMessageType](s"$msgtype is not supported")
    }
  }

  implicit val roomMessageEmptyEncoder: JsonEncoder[RoomMessageEmpty.type] =
    DeriveJsonEncoder.gen[RoomMessageEmpty.type]

  implicit val roomTextContentEncoder: JsonEncoder[RoomMessageTextContent] =
    Json.encoder.contramap[RoomMessageTextContent] { message =>
      val json = DeriveJsonEncoder.gen[RoomMessageTextContent].toJsonAST(message)
      json
        .map(_.merge(Json.Obj("msgtype" -> Json.Str(message.msgtype))))
        .getOrElse(Json.Obj())
    }
  implicit val roomImageContentEncoder: JsonEncoder[RoomMessageImageContent] =
    Json.encoder.contramap[RoomMessageImageContent] { message =>
      val json = DeriveJsonEncoder.gen[RoomMessageImageContent].toJsonAST(message)
      json
        .map(_.merge(Json.Obj("msgtype" -> Json.Str(message.msgtype))))
        .getOrElse(Json.Obj())
    }

}
