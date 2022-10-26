package com.bot4s.zmatrix.models

import io.circe.generic.extras.semiauto._
import io.circe.{ Decoder, Json }

import RoomMessageType._

/**
 * Matrix Event class for events in rooms
 * This is an implementation for https://spec.matrix.org/v1.3/client-server-api/#instant-messaging
 *
 * Those events does not have all the fields that similar events
 * have for the joined rooms.
 *
 * Is Matrix' documentation, this is often refered to as StrippedState.
 */
sealed trait RoomEvent

// m.room.message event
final case class MessageEvent(sender: String, eventId: String, content: RoomMessageType) extends RoomEvent {
  val `type`: String = "m.room.message"
}

// m.room.topic event
final case class TopicEvent(sender: String, eventId: String, content: TopicMessageContent) extends RoomEvent {
  val `type`: String = "m.room.topic"
}
final case class TopicMessageContent(topic: String)

object RoomEvent {
  final case class UnknownEvent(
    `type`: String,
    content: Json
  ) extends RoomEvent

  implicit val roomMessageDecoder: Decoder[MessageEvent]                = deriveConfiguredDecoder
  implicit val topicMessageDecoder: Decoder[TopicEvent]                 = deriveConfiguredDecoder
  implicit val topicMessageContentDecoder: Decoder[TopicMessageContent] = deriveConfiguredDecoder

  /* Those decoder are used by the sync state */
  implicit val roomEventDecoder: Decoder[RoomEvent] = c =>
    c.downField("type").as[String].flatMap { label =>
      label match {
        case "m.room.message" => c.as[MessageEvent]
        case "m.room.topic"   => c.as[TopicEvent]
        case "m.room.member"  => Right(UnknownEvent(label, c.value)) // another kind of room message, as a placeholder
        case _                => Right(UnknownEvent(label, c.value))
      }
    }

}
