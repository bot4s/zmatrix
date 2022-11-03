package com.bot4s.zmatrix

import zio.Console._
import zio._
import com.bot4s.zmatrix.models.InviteEvent._
import com.bot4s.zmatrix.models.RoomMessageType._
import com.bot4s.zmatrix.models._
import com.bot4s.zmatrix.services.Authentication

object Simple2 extends ExampleApp[Unit] {

  def pollLoop(fromBot: String => Boolean) =
    Matrix.sync.tapInviteEvent {
      case (roomId, invite: InviteMemberEvent) if invite.content.membership == "invite" =>
        ZIO.logInfo(f"Joining $roomId") *> Matrix.join(roomId)
    }.tapRoomEvent {
      case (roomId, TopicEvent(sender, _, TopicMessageContent(topic))) =>
        Matrix.sendMsg(roomId, s"Thanks $sender for setting the topic to '${topic}'")

      case (roomId, MessageEvent(sender, _, content: RoomMessageType.RoomMessageTextContent)) if !fromBot(sender) =>
        ZIO.logInfo(f"${roomId} Message received: ${content.body}") *>
          Matrix.sendMsg(roomId, "welcome back")

      case (roomId, MessageEvent(sender, _, content: RoomMessageImageContent)) if !fromBot(sender) =>
        ZIO.logInfo(f"${roomId} Image received: ${content.body}") *>
          Matrix.sendEvent(roomId, RoomMessageTextContent("welcome back (image)"))
    }.updateState()

  override def runExample: ZIO[Matrix with AuthMatrixEnv, MatrixError, Unit] = for {
    config <- MatrixConfiguration.get
    _      <- Authentication.refresh
    _      <- Matrix.whoAmI.flatMap(c => printLine(c.toString()).ignore)
    fromBot = (sender: String) => config.matrix.userId.exists(name => sender.startsWith(s"@$name"))
    _      <- pollLoop(fromBot).repeat(Schedule.spaced(10.seconds))
  } yield ()
}
