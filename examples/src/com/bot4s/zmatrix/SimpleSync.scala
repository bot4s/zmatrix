package com.bot4s.zmatrix

import zio._
import com.bot4s.zmatrix.api.{ roomMembership, rooms, sync }
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.InviteEvent._
import com.bot4s.zmatrix.models.RoomEvent._

object SimpleSync extends ExampleApp[Long] {

  override def runExample: ZIO[AuthMatrixEnv, MatrixError, Long] = {
    val method: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
      sync.sync.tapInviteEvent {
        case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
          ZIO.logInfo(f"Joining $roomId") *> roomMembership.join(roomId)
      }.tapRoomEvent { case (roomId, msg: RoomMessageText) =>
        val notFromBot =
          MatrixConfiguration.get.map(!_.matrix.userId.exists(botName => msg.sender.startsWith(s"@$botName")))
        ZIO.whenZIO(notFromBot)(
          ZIO.logInfo(f"${roomId} Message received: ${msg.content.body}") *>
            rooms.sendMsg(
              roomId,
              EventType.roomMessages,
              "welcome back"
            )
        )
      }.updateState()

    method
      .repeat(Schedule.spaced(10.seconds))

  }

}
