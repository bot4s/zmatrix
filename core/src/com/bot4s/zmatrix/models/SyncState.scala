package com.bot4s.zmatrix.models

import io.circe.{ Decoder, KeyDecoder }
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class RoomId(id: String) extends AnyVal

object StateDecoder {
  implicit val roomIdDecoder: Decoder[RoomId] =
    deriveConfiguredDecoder[RoomId] or Decoder[String].map(x => RoomId(x))

  implicit val roomIdKeyDecoder: KeyDecoder[RoomId] = new KeyDecoder[RoomId] {
    override def apply(key: String): Some[RoomId] = Some(RoomId(key))
  }

  implicit val syncStateDecoder: Decoder[SyncState]     = deriveConfiguredDecoder
  implicit val roomsDecoder: Decoder[Rooms]             = deriveConfiguredDecoder
  implicit val roomEventDecoder: Decoder[RoomEvent]     = deriveConfiguredDecoder
  implicit val inviteEventDecoder: Decoder[InviteEvent] = deriveConfiguredDecoder
  implicit val accountDataDecoder: Decoder[AccountData] = deriveConfiguredDecoder
  implicit val eventDecoder: Decoder[Event]             = deriveConfiguredDecoder
}

final case class SyncState(
  accountData: Option[AccountData],
  nextBatch: String,
  rooms: Option[Rooms]
)

final case class Rooms(
  invite: Option[Map[RoomId, InviteEventWrapper]],
  join: Option[Map[RoomId, RoomEventWrapper]]
  // leave: Option[Map[String, LeaveEvent]]
)

final case class AccountData(
  events: List[Event]
)

final case class Event(
  `type`: String
)
