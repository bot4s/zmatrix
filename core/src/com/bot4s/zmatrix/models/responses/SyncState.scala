package com.bot4s.zmatrix.models.responses

import com.bot4s.zmatrix.models._
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

/*
  Model the response from the sync endpoint as documented in
  https://spec.matrix.org/v1.4/client-server-api/#get_matrixclientv3sync
 */
final case class SyncState(
  accountData: Option[AccountData],
  nextBatch: String,
  rooms: Option[Rooms]
)

final case class AccountData(events: List[Event])
final case class Event(`type`: String)

object SyncState {
  implicit val eventDecoder: Decoder[Event]             = deriveConfiguredDecoder
  implicit val accountDataDecoder: Decoder[AccountData] = deriveConfiguredDecoder

  implicit val syncStateDecoder: Decoder[SyncState] = deriveConfiguredDecoder
}
