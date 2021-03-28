package com.bot4s.zmatrix.api

import com.bot4s.zmatrix.models.RoomId
import com.bot4s.zmatrix.models.responses.EventResponse
import io.circe.Json
import java.util.UUID
import com.bot4s.zmatrix.models.EventType.EventType

trait Rooms {

  /*
   * Send a message event to a room
   * Documentation: https://matrix.org/docs/spec/client_server/latest#put-matrix-client-r0-rooms-roomid-send-eventtype-txnid
   */
  def sendMsg(roomId: RoomId, eventType: EventType, text: String) =
    (putJson(
      Seq("rooms", roomId.id, "send", eventType.toString(), UUID.randomUUID().toString()),
      Json.obj(
        "msgtype" -> Json.fromString("m.text"),
        "body"    -> Json.fromString(text)
      )
    ) >>= authenticate >>= send) >>= as[EventResponse]

}

object rooms extends Rooms
