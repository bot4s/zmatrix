package com.bot4s.zmatrix.models

import zio.json._
import zio.json.ast._

/**
 * Matrix Event class for events in invited rooms
 *
 * Those events does not have all the fields that similar events
 * have for the joined rooms.
 *
 * Is Matrix' documentation, this is often refered to as StrippedState.
 */
sealed trait InviteEvent

object InviteEvent {
  // This corresponds to an m.room.member event
  final case class InviteMemberEvent(
    sender: String,
    @jsonField("state_key") stateKey: String,
    @jsonField("event_id") eventId: Option[String],
    content: InviteMemberEventContent
  ) extends InviteEvent

  final case class InviteMemberEventContent(
    membership: String,
    displayname: String
  )

  final case class GenericMemberEventContent(
    `type`: String,
    content: Json
  ) extends InviteEvent

  implicit val inviteMemberEventContentDecoder: JsonDecoder[InviteMemberEventContent] = DeriveJsonDecoder.gen
  implicit val inviteMemberEventDecoder: JsonDecoder[InviteMemberEvent]               = DeriveJsonDecoder.gen

  implicit val inviteEventDecoder: JsonDecoder[InviteEvent] = JsonDecoder[Json].mapOrFail { json =>
    json.get(JsonCursor.field("type")).flatMap(_.as[String]).flatMap {
      case "m.room.member" => json.as[InviteMemberEvent]
      case label           => Right(GenericMemberEventContent(label, json))

    }
  }
}
