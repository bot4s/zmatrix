package com.bot4s.zmatrix.models

import io.circe.Decoder
import io.circe.generic.extras.semiauto._

final case class Rooms(
  invite: Option[Map[RoomId, InvitedRoom]],
  join: Option[Map[RoomId, JoinedRoom]]
  // leave: Option[Map[String, LeaveEvent]]
)

final case class JoinedRoom(timeline: RoomEventTimeline)
final case class RoomEventTimeline(events: List[RoomEvent], limited: Boolean)

final case class InvitedRoom(inviteState: InviteState)
final case class InviteState(events: List[InviteEvent])

object Rooms {
  implicit val roomsDecoder: Decoder[Rooms] = deriveConfiguredDecoder

  implicit val joinedRoomDecoder: Decoder[JoinedRoom]             = deriveConfiguredDecoder
  implicit val joinedRoomEventDecoder: Decoder[RoomEventTimeline] = deriveConfiguredDecoder
  implicit val invitedRoomDecoder: Decoder[InvitedRoom]           = deriveConfiguredDecoder
  implicit val invitedRoomStateDecoder: Decoder[InviteState]      = deriveConfiguredDecoder
}
