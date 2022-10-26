package com.bot4s.zmatrix

import zio._

import com.bot4s.zmatrix.api.{ roomMembership, rooms, sync }
import com.bot4s.zmatrix.models.InviteEvent._
import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.models.responses.SyncState

object SimpleSync extends ExampleApp[Long] {

  override def runExample: ZIO[AuthMatrixEnv, MatrixError, Long] = {
    val method: ZIO[AuthMatrixEnv, MatrixError, SyncState] =
      sync.sync.tapInviteEvent {
        case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
          ZIO.logInfo(f"Joining $roomId") *> roomMembership.join(roomId)
      }.tapRoomEvent {
        case (roomId, TopicEvent(sender, _, TopicMessageContent(topic))) =>
          rooms.sendMsg(roomId, s"Thanks $sender for setting the topic to '${topic}'")

        case (roomId, MessageEvent(sender, _, content: RoomMessageTextContent)) =>
          val notFromBot =
            MatrixConfiguration.get.map(!_.matrix.userId.exists(botName => sender.startsWith(s"@$botName")))
          ZIO.whenZIO(notFromBot)(
            ZIO.logInfo(f"${roomId} Message received: ${content.body}") *>
              rooms.sendMsg(roomId, "welcome back")
          )

        case (roomId, MessageEvent(sender, _, content: RoomMessageImageContent)) =>
          val notFromBot =
            MatrixConfiguration.get.map(!_.matrix.userId.exists(botName => sender.startsWith(s"@$botName")))
          ZIO.whenZIO(notFromBot)(
            ZIO.logInfo(f"${roomId} Image received: ${content.body}") *>
              rooms.sendMsg(roomId, RoomMessageTextContent("welcome back (image)"))
          )
      }.updateState()

    method
      .repeat(Schedule.spaced(10.seconds))

  }

}
