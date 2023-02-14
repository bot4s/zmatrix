package com.bot4s.zmatrix

import zio._
import com.bot4s.zmatrix.stream._
import com.bot4s.zmatrix.models._

object StreamSync extends ExampleApp[Unit] {

  val greetings =
    messageEvents >>> filterBotMessage >>> onMessageEventZIO { case (roomId, message) =>
      Matrix.sendMsg(roomId, s"Welcome back ${message.sender}")
    }

  val detectTopicChange = onRoomEventZIO { case (roomId, TopicEvent(sender, _, TopicMessageContent(topic))) =>
    Matrix.sendMsg(roomId, s"Thanks $sender for setting the topic to '${topic}'")
  }

  val handleInvite = onInviteEventZIO {
    case (roomId, invite: InviteEvent.InviteMemberEvent) if invite.content.membership == "invite" =>
      ZIO.logInfo(f"Joining $roomId") *> Matrix.join(roomId)
  }

  val source = syncedSource
    .repeat(Schedule.spaced(10.seconds))

  val runExample =
    Matrix.whoAmI.debug *> addCallbacks(
      source,
      List(
        handleInvite,
        greetings,
        detectTopicChange
      )
    )

}
