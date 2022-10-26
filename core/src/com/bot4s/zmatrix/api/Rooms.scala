package com.bot4s.zmatrix.api

import java.util.UUID

import com.bot4s.zmatrix.models.responses.EventResponse
import com.bot4s.zmatrix.models.{ EventType, RoomId, RoomMessageType }
import com.bot4s.zmatrix.models.RoomMessageType._
import io.circe.syntax._

trait Rooms {

  /*
   * Send a message event to a room
   * Documentation: https://matrix.org/docs/spec/client_server/latest#put-matrix-client-r0-rooms-roomid-send-eventtype-txnid
   * https://spec.matrix.org/latest/client-server-api/#mtext
   */
  def sendMsg(roomId: RoomId, messageEvent: RoomMessageType) =
    sendWithAuth[EventResponse](
      putJson(
        Seq("rooms", roomId.id, "send", EventType.roomMessages.toString(), UUID.randomUUID().toString()),
        messageEvent.asJson
      )
    )

  def sendMsg(roomId: RoomId, message: String) =
    sendWithAuth[EventResponse](
      putJson(
        Seq("rooms", roomId.id, "send", EventType.roomMessages.toString(), UUID.randomUUID().toString()),
        RoomMessageTextContent(message).asJson
      )
    )

}

object rooms extends Rooms
