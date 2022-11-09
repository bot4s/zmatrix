package com.bot4s.zmatrix.models.responses

import zio.json._

import com.bot4s.zmatrix.models._

/*
  Model the response from the sync endpoint as documented in
  https://spec.matrix.org/v1.4/client-server-api/#get_matrixclientv3sync
 */
final case class SyncState(
  @jsonField("account_data") accountData: Option[AccountData],
  @jsonField("next_batch") nextBatch: String,
  rooms: Option[Rooms]
)

final case class AccountData(events: List[Event])
final case class Event(`type`: String)

object SyncState {
  implicit val eventDecoder: JsonDecoder[Event]             = DeriveJsonDecoder.gen
  implicit val accountDataDecoder: JsonDecoder[AccountData] = DeriveJsonDecoder.gen

  implicit val syncStateDecoder: JsonDecoder[SyncState] = DeriveJsonDecoder.gen
}
