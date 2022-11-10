package com.bot4s.zmatrix.models

import zio.json._
import zio.json.ast._

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
final case class MessageEvent(sender: String, @jsonField("event_id") eventId: String, content: RoomMessageType)
    extends RoomEvent {
  val `type`: String = "m.room.message"
}

// m.room.topic event
final case class TopicEvent(sender: String, @jsonField("event_id") eventId: String, content: TopicMessageContent)
    extends RoomEvent {
  val `type`: String = "m.room.topic"
}
final case class TopicMessageContent(topic: String)

object RoomEvent {
  final case class UnknownEvent(
    `type`: String,
    content: Json
  ) extends RoomEvent

  implicit val roomMessageDecoder: JsonDecoder[MessageEvent]                = DeriveJsonDecoder.gen
  implicit val topicMessageContentDecoder: JsonDecoder[TopicMessageContent] = DeriveJsonDecoder.gen
  implicit val topicMessageDecoder: JsonDecoder[TopicEvent]                 = DeriveJsonDecoder.gen

  implicit val roomEventDecoder: JsonDecoder[RoomEvent] = JsonDecoder[Json].mapOrFail { json =>
    json.get(JsonCursor.field("type")).flatMap(_.as[String]).flatMap {
      case "m.room.message" => json.as[MessageEvent]
      case "m.room.topic"   => json.as[TopicEvent]
      case label            => Right[String, RoomEvent](UnknownEvent(label, json))
    }
  }

}
