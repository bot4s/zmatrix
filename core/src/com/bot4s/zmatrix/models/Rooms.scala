package com.bot4s.zmatrix.models

import zio.json._

final case class Rooms(
  invite: Option[Map[RoomId, InvitedRoom]],
  join: Option[Map[RoomId, JoinedRoom]]
  // leave: Option[Map[String, LeaveEvent]]
)

final case class JoinedRoom(timeline: RoomEventTimeline)
final case class RoomEventTimeline(events: List[RoomEvent], limited: Boolean)

final case class InvitedRoom(@jsonField("invite_state") inviteState: InviteState)
final case class InviteState(events: List[InviteEvent])

object Rooms {

  implicit lazy val joinedRoomEventDecoder: JsonDecoder[RoomEventTimeline] = DeriveJsonDecoder.gen
  implicit lazy val joinedRoomDecoder: JsonDecoder[JoinedRoom]             = DeriveJsonDecoder.gen
  implicit lazy val invitedRoomStateDecoder: JsonDecoder[InviteState]      = DeriveJsonDecoder.gen
  implicit lazy val invitedRoomDecoder: JsonDecoder[InvitedRoom]           = DeriveJsonDecoder.gen

  implicit lazy val roomsDecoder: JsonDecoder[Rooms] = DeriveJsonDecoder.gen
}
