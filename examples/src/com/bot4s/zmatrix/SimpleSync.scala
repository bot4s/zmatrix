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
      for {
        config <- MatrixConfiguration.get
        fromBot = (sender: String) => config.matrix.userId.exists(name => sender.startsWith(s"@$name"))
        nextState <- sync.sync.tapInviteEvent {
                       case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
                         ZIO.logInfo(f"Joining $roomId") *> roomMembership.join(roomId)
                     }.tapRoomEvent {
                       case (roomId, TopicEvent(sender, _, TopicMessageContent(topic))) =>
                         rooms.sendMsg(roomId, s"Thanks $sender for setting the topic to '${topic}'")

                       case (roomId, MessageEvent(sender, _, content: RoomMessageTextContent)) if !fromBot(sender) =>
                         ZIO.logInfo(f"${roomId} Message received: ${content.body}") *>
                           rooms.sendMsg(roomId, "welcome back")

                       case (roomId, MessageEvent(sender, _, content: RoomMessageImageContent)) if !fromBot(sender) =>
                         ZIO.logInfo(f"${roomId} Image received: ${content.body}") *>
                           rooms.sendEvent(roomId, RoomMessageTextContent("welcome back (image)"))
                     }.updateState()
      } yield nextState

    method
      .repeat(Schedule.spaced(10.seconds))

  }

}
