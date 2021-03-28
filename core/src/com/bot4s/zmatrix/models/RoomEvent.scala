package com.bot4s.zmatrix.models

import io.circe.{ Decoder, HCursor, Json }
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class RoomEventWrapper(
  timeline: RoomEventWrapperContent
)

final case class RoomEventWrapperContent(
  events: List[RoomEvent],
  limited: Boolean
)

object RoomEventWrapper {
  implicit val roomEventWrapperDecoder: Decoder[RoomEventWrapper]               = deriveConfiguredDecoder
  implicit val roomEventWrapperContentDecoder: Decoder[RoomEventWrapperContent] = deriveConfiguredDecoder
}

/**
 * Matrix Event class for events in invited rooms
 *
 * Those events does not have all the fields that similar events
 * have for the joined rooms.
 *
 * Is Matrix' documentation, this is often refered to as StrippedState.
 */
sealed trait RoomEvent

object RoomEvent {
  // This corresponds to an m.room.member event
  final case class RoomMessageText(
    sender: String,
    eventId: String,
    content: RoomMessageTextContent,
    `type`: String = "m.room.message"
  ) extends RoomEvent

  final case class RoomMessageTextContent(
    msgtype: String = "m.text",
    body: String
  )

  final case class UnknownEvent(
    `type`: String,
    content: Json
  ) extends RoomEvent

  implicit val roomMessageTextDecoder: Decoder[RoomMessageText]               = deriveConfiguredDecoder
  implicit val roomMessageTextContentDecoder: Decoder[RoomMessageTextContent] = deriveConfiguredDecoder

  implicit val unknownEventDecoder: Decoder[UnknownEvent] = deriveConfiguredDecoder

  implicit val roomEventDecoder: Decoder[RoomEvent] = new Decoder[RoomEvent] {
    def apply(c: HCursor): Decoder.Result[RoomEvent] =
      c.downField("type").as[String].flatMap { label =>
        label match {
          case "m.room.message" => roomMessageDecoder(c)
          case _                => Right(UnknownEvent(label, c.value))
        }
      }
  }

  val roomMessageDecoder = new Decoder[RoomEvent] {
    def apply(c: HCursor): Decoder.Result[RoomEvent] =
      c.downField("content").downField("msgtype").as[String].flatMap { msgType =>
        msgType match {
          case "m.text" => c.as[RoomMessageText]
          case _        => Right(UnknownEvent(f"m.room.message/$msgType", c.value))
        }
      }
  }
}
