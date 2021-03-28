package com.bot4s.zmatrix.models

import io.circe.{ Decoder, HCursor, Json }
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class InviteEventWrapper(
  inviteState: InviteEventWrapperContent
)

final case class InviteEventWrapperContent(
  events: List[InviteEvent]
)

object InviteEventWrapper {
  implicit val inviteEventWrapperDecoder: Decoder[InviteEventWrapper]               = deriveConfiguredDecoder
  implicit val inviteEventWrapperDecoderContent: Decoder[InviteEventWrapperContent] = deriveConfiguredDecoder
}

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
    stateKey: String,
    eventId: Option[String],
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

  implicit val inviteMemberEventDecoder: Decoder[InviteMemberEvent]               = deriveConfiguredDecoder
  implicit val inviteMemberEventContentDecoder: Decoder[InviteMemberEventContent] = deriveConfiguredDecoder

  implicit val inviteEventDecoder: Decoder[InviteEvent] = new Decoder[InviteEvent] {
    def apply(c: HCursor): Decoder.Result[InviteEvent] =
      c.downField("type").as[String].flatMap { label =>
        label match {
          case "m.room.member" => c.as[InviteMemberEvent]
          case _               => Right(GenericMemberEventContent(label, c.value))
        }

      }
  }
}
