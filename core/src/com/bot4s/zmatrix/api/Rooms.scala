package com.bot4s.zmatrix.api

import zio.ZIO
import zio.json._

import java.util.UUID

import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models.responses.EventResponse
import com.bot4s.zmatrix.models.{ EventType, RoomId, RoomMessageType }
import com.bot4s.zmatrix.{ Matrix, MatrixApiBase }

trait Rooms { self: MatrixApiBase =>

  /*
   * Send a message event to a room
   * Documentation: https://matrix.org/docs/spec/client_server/latest#put-matrix-client-r0-rooms-roomid-send-eventtype-txnid
   * https://spec.matrix.org/latest/client-server-api/#mtext
   */
  def sendEvent(roomId: RoomId, messageEvent: RoomMessageType) =
    ZIO.logDebug(messageEvent.toJson) *>
      sendWithAuth[EventResponse](
        putJson(
          Seq("rooms", roomId.id, "send", EventType.roomMessages.toString(), UUID.randomUUID().toString()),
          messageEvent
        )
      )

  def sendMsg(roomId: RoomId, message: String) =
    sendWithAuth[EventResponse](
      putJson(
        Seq("rooms", roomId.id, "send", EventType.roomMessages.toString(), UUID.randomUUID().toString()),
        RoomMessageTextContent(message)
      )
    )

}

private[zmatrix] trait RoomAccessors {
  def sendEvent(roomId: RoomId, messsageEvent: RoomMessageType) =
    ZIO.serviceWithZIO[Matrix](_.sendEvent(roomId, messsageEvent))
  def sendMsg(roomId: RoomId, message: String) =
    ZIO.serviceWithZIO[Matrix](_.sendMsg(roomId, message))
}
