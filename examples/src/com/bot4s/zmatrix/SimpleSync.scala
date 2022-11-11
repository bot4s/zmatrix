package com.bot4s.zmatrix

import zio._

import com.bot4s.zmatrix.models.InviteEvent._
import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models._

object SimpleSync extends ExampleApp[Unit] {

  def pollLoop(fromBot: String => Boolean) =
    Matrix.sync.tapInviteEvent {
      case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
        ZIO.logInfo(f"Joining $roomId") *> Matrix.join(roomId)
    }.tapRoomEvent {
      case (roomId, TopicEvent(sender, _, TopicMessageContent(topic))) =>
        Matrix.sendMsg(roomId, s"Thanks $sender for setting the topic to '${topic}'")

      case (roomId, MessageEvent(sender, _, content: RoomMessageTextContent)) if !fromBot(sender) =>
        ZIO.logInfo(f"${roomId} Message received: ${content.body}") *>
          Matrix.sendMsg(roomId, "welcome back")

      case (roomId, MessageEvent(sender, _, content: RoomMessageImageContent)) if !fromBot(sender) =>
        ZIO.logInfo(f"${roomId} Image received: ${content.body}") *>
          Matrix.sendEvent(roomId, RoomMessageTextContent("welcome back (image)"))
    }.updateState()

  override def runExample: ZIO[AuthMatrixEnv, MatrixError, Unit] =
    for {
      _      <- Matrix.whoAmI.debug
      config <- ZIO.service[MatrixConfiguration]
      fromBot = (sender: String) => config.matrix.userId.exists(name => sender.startsWith(s"@$name"))
      _      <- pollLoop(fromBot).repeat(Schedule.spaced(10.seconds))
    } yield ()

}
